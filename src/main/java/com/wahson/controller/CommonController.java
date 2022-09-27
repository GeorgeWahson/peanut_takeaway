package com.wahson.controller;

import com.wahson.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${peanut.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file) {
        // file是一个临时文件，需要转存到指定位置，否则本次请求后临时文件会删除
        log.info("file.toString():  {}", file.toString());
        // 使用UUID重新生成文件名，防止名称重复
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 判断目录是否存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdir();
        }



        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName + suffix));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.success(fileName + suffix);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // TODO try(resource)
            // 输入流，读取文件
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，写回浏览器，显示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
