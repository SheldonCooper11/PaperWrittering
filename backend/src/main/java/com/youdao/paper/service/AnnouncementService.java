package com.youdao.paper.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youdao.paper.common.ResultCode;
import com.youdao.paper.entity.Announcement;
import com.youdao.paper.exception.BusinessException;
import com.youdao.paper.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    public List<Announcement> listPublished() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreateTime));
    }

    public List<Announcement> listAll() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime));
    }

    public Announcement create(Announcement announcement) {
        announcementMapper.insert(announcement);
        return announcement;
    }

    public Announcement update(Long id, Announcement announcement) {
        Announcement exist = announcementMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "公告不存在");
        }
        exist.setTitle(announcement.getTitle());
        exist.setContent(announcement.getContent());
        exist.setStatus(announcement.getStatus());
        announcementMapper.updateById(exist);
        return exist;
    }

    public void delete(Long id) {
        if (announcementMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "公告不存在");
        }
    }
}
