package com.marmot.qilu.modules.media.service;

import com.marmot.qilu.modules.media.vo.MediaFileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaFileService {

    MediaFileUploadVO uploadPostImage(MultipartFile file);

    int markMediaFilesUsed(List<Long> mediaIds);
}
