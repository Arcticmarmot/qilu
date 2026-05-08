package com.marmot.qilu.modules.media.service;

import com.marmot.qilu.modules.media.vo.MediaUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {

    MediaUploadVO uploadPostImage(MultipartFile file);
}
