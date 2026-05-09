package com.marmot.qilu.modules.media.service;

import com.marmot.qilu.modules.media.vo.MediaFileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface MediaFileService {

    MediaFileUploadVO uploadPostImage(MultipartFile file);
}
