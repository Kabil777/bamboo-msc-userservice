package com.bamboo.userService.service;

import com.bamboo.userService.common.enums.Visibility;
import com.bamboo.userService.common.model.VisibilityCount;
import com.bamboo.userService.dto.ContentCountEvent;
import com.bamboo.userService.dto.UserCountSummaryDto;
import com.bamboo.userService.entity.UserCount;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.repository.UserCountRepository;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserCountService {
    private final UserCountRepository userCountRepository;
    private final UserRepository userRepository;
    private final HandleService handleService;

    public UserCountService(
            UserCountRepository userCountRepository,
            UserRepository userRepository,
            HandleService handleService) {
        this.userCountRepository = userCountRepository;
        this.userRepository = userRepository;
        this.handleService = handleService;
    }

    @Transactional
    public UserCount ensureExists(UUID userId) {
        return userCountRepository
                .findById(userId)
                .orElseGet(() -> userCountRepository.save(UserCount.builder().userId(userId).build()));
    }

    @Transactional
    public void incrementFollow(UUID followerId, UUID followingId) {
        UserCount followerCounts = ensureExists(followerId);
        UserCount followingCounts = ensureExists(followingId);

        followerCounts.setFollowingCount(followerCounts.getFollowingCount() + 1);
        followingCounts.setFollowersCount(followingCounts.getFollowersCount() + 1);
    }

    @Transactional
    public void decrementFollow(UUID followerId, UUID followingId) {
        UserCount followerCounts = ensureExists(followerId);
        UserCount followingCounts = ensureExists(followingId);

        followerCounts.setFollowingCount(Math.max(0, followerCounts.getFollowingCount() - 1));
        followingCounts.setFollowersCount(Math.max(0, followingCounts.getFollowersCount() - 1));
    }

    @Transactional
    public void applyContentCountEvent(ContentCountEvent event) {
        if (event == null || event.userId() == null || event.contentType() == null || event.action() == null) {
            return;
        }

        UserCount userCount = ensureExists(event.userId());
        VisibilityCount current =
                switch (event.contentType()) {
                    case "BLOG" -> userCount.getBlogCounts();
                    case "DOCS" -> userCount.getDocsCounts();
                    default -> null;
                };

        if (current == null) {
            return;
        }

        VisibilityCount updated =
                switch (event.action()) {
                    case "CREATED" -> incrementVisibility(current, event.newVisibility());
                    case "DELETED" -> decrementVisibility(current, event.oldVisibility());
                    case "VISIBILITY_CHANGED" -> updateVisibility(current, event.oldVisibility(), event.newVisibility());
                    default -> current;
                };

        if ("BLOG".equals(event.contentType())) {
            userCount.setBlogCounts(updated);
        } else if ("DOCS".equals(event.contentType())) {
            userCount.setDocsCounts(updated);
        }
    }

    @Transactional(readOnly = true)
    public UserCountSummaryDto getSummaryByHandle(String handle) {
        UUID userId =
                userRepository
                        .findByHandleIgnoreCase(handleService.normalizeHandle(handle))
                        .map(UserModel::getId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return getSummaryByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserCountSummaryDto getSummaryByHandleForViewer(String handle, UUID requesterId) {
        UUID userId =
                userRepository
                        .findByHandleIgnoreCase(handleService.normalizeHandle(handle))
                        .map(UserModel::getId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserCountSummaryDto summary = getSummaryByUserId(userId);
        if (requesterId != null && requesterId.equals(userId)) {
            return summary;
        }

        return sanitizeForPublicView(summary);
    }

    @Transactional(readOnly = true)
    public UserCountSummaryDto getSummaryByUserId(UUID userId) {
        UserCount userCount =
                userCountRepository.findById(userId).orElseGet(() -> UserCount.builder().userId(userId).build());

        return new UserCountSummaryDto(
                userCount.getFollowersCount(),
                userCount.getFollowingCount(),
                userCount.getBlogCounts(),
                userCount.getDocsCounts(),
                new HashMap<>(userCount.getOtherCounts()));
    }

    private UserCountSummaryDto sanitizeForPublicView(UserCountSummaryDto summary) {
        return new UserCountSummaryDto(
                summary.followers(),
                summary.following(),
                toPublicVisibilityCount(summary.blogs()),
                toPublicVisibilityCount(summary.docs()),
                new HashMap<>(summary.otherCounts()));
    }

    private VisibilityCount toPublicVisibilityCount(VisibilityCount count) {
        if (count == null) {
            return VisibilityCount.empty();
        }

        return new VisibilityCount(count.publicCount(), count.publicCount(), 0);
    }

    private VisibilityCount incrementVisibility(VisibilityCount count, Visibility visibility) {
        long publicCount = count.publicCount();
        long privateCount = count.privateCount();

        if (visibility == Visibility.PUBLIC) {
            publicCount++;
        } else if (visibility == Visibility.PRIVATE) {
            privateCount++;
        }

        return new VisibilityCount(count.total() + 1, publicCount, privateCount);
    }

    private VisibilityCount decrementVisibility(VisibilityCount count, Visibility visibility) {
        long publicCount = count.publicCount();
        long privateCount = count.privateCount();

        if (visibility == Visibility.PUBLIC) {
            publicCount = Math.max(0, publicCount - 1);
        } else if (visibility == Visibility.PRIVATE) {
            privateCount = Math.max(0, privateCount - 1);
        }

        return new VisibilityCount(Math.max(0, count.total() - 1), publicCount, privateCount);
    }

    private VisibilityCount updateVisibility(
            VisibilityCount count, Visibility oldVisibility, Visibility newVisibility) {
        if (oldVisibility == newVisibility) {
            return count;
        }
        return incrementVisibility(decrementVisibilityKeepTotal(count, oldVisibility), newVisibility, count.total());
    }

    private VisibilityCount decrementVisibilityKeepTotal(VisibilityCount count, Visibility visibility) {
        long publicCount = count.publicCount();
        long privateCount = count.privateCount();

        if (visibility == Visibility.PUBLIC) {
            publicCount = Math.max(0, publicCount - 1);
        } else if (visibility == Visibility.PRIVATE) {
            privateCount = Math.max(0, privateCount - 1);
        }

        return new VisibilityCount(count.total(), publicCount, privateCount);
    }

    private VisibilityCount incrementVisibility(
            VisibilityCount count, Visibility visibility, long totalOverride) {
        long publicCount = count.publicCount();
        long privateCount = count.privateCount();

        if (visibility == Visibility.PUBLIC) {
            publicCount++;
        } else if (visibility == Visibility.PRIVATE) {
            privateCount++;
        }

        return new VisibilityCount(totalOverride, publicCount, privateCount);
    }
}
