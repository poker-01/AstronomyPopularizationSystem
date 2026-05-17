package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Comment;
import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModerationItemDto {

    private Long id;
    private String type;
    private Long postId;
    private Long userId;
    private String authorName;
    private String title;
    private String content;
    private ModerationStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static ModerationItemDto fromPost(Post post, String authorName) {
        ModerationItemDto dto = new ModerationItemDto();
        dto.setId(post.getId());
        dto.setType("POST");
        dto.setUserId(post.getUserId());
        dto.setAuthorName(authorName);
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setRejectReason(post.getRejectReason());
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }

    public static ModerationItemDto fromComment(Comment comment, String authorName, String postTitle) {
        ModerationItemDto dto = new ModerationItemDto();
        dto.setId(comment.getId());
        dto.setType("COMMENT");
        dto.setPostId(comment.getPostId());
        dto.setUserId(comment.getUserId());
        dto.setAuthorName(authorName);
        dto.setTitle(postTitle);
        dto.setContent(comment.getContent());
        dto.setStatus(comment.getStatus());
        dto.setRejectReason(comment.getRejectReason());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
