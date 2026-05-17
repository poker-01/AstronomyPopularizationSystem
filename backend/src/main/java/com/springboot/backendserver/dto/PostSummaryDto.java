package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostSummaryDto {

    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String contentPreview;
    private ModerationStatus status;
    private String rejectReason;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;

    public static PostSummaryDto from(Post post, String authorName, String authorAvatar) {
        PostSummaryDto dto = new PostSummaryDto();
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setAuthorName(authorName);
        dto.setAuthorAvatar(authorAvatar);
        dto.setTitle(post.getTitle());
        String content = post.getContent();
        if (content != null && content.length() > 120) {
            dto.setContentPreview(content.substring(0, 120) + "…");
        } else {
            dto.setContentPreview(content);
        }
        dto.setStatus(post.getStatus());
        dto.setRejectReason(post.getRejectReason());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }
}
