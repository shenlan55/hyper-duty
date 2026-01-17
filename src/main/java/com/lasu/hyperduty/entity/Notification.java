package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long receiverId;

    private String receiverName;

    private String title;

    private String content;

    private String type;

    private String status;

    private Long relatedId;

    private String relatedType;

    private LocalDateTime createTime;

    private LocalDateTime readTime;
}
