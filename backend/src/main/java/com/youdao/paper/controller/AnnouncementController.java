package com.youdao.paper.controller;

import com.youdao.paper.common.ResultVO;
import com.youdao.paper.entity.Announcement;
import com.youdao.paper.service.AnnouncementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResultVO<List<Announcement>> listPublished() {
        return ResultVO.success(announcementService.listPublished());
    }
}
