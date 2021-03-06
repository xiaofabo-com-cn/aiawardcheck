package cn.com.xiaofabo.scia.aiawardcheck.controller;

import cn.com.xiaofabo.scia.aiawardcheck.entity.Award;
import cn.com.xiaofabo.scia.aiawardcheck.util.CommonUtil;
import cn.com.xiaofabo.scia.aiawardcheck.common.ApiResult;
import cn.com.xiaofabo.scia.aiawardcheck.common.ResultCodeEnum;
import cn.com.xiaofabo.scia.aiawardcheck.fileprocessor.AwardReader;
import cn.com.xiaofabo.scia.aiawardcheck.fileprocessor.AwardWriter;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Controller
public class RestController {

    @RequestMapping("/hello")
    @ResponseBody
    public Object hello(){
        return "hello AI";
    }

    @RequestMapping("/formatcheckservice")
    @ResponseBody
    public Object formatcheck(@RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            return ApiResult.resultWith(ResultCodeEnum.FILE_INPUT_NOT_FOUND);
        }

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = (FileInputStream) file.getInputStream();
            if (file.getInputStream() == null){
                return ApiResult.resultWith(ResultCodeEnum.FILE_IS_EMPTY);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResult.resultWith(ResultCodeEnum.SYSTEM_INPUT_IO_EXCEPTION);
        }

        AwardReader arbReader = new AwardReader();

        String tempFilePath = ClassUtils.getDefaultClassLoader().getResource("").getPath()+"/temp.doc";

        writeToFile(fileInputStream, tempFilePath);
        File tempFile = new File(tempFilePath);
        Award ab = null;
        try {
            ab = arbReader.buildAward(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResult.resultWith(ResultCodeEnum.SYSTEM_READ_FAIL);
        }

        String outPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String outFileName = "out.docx";
        String outFileUrl = outPath + outFileName;

        AwardWriter arbWriter = new AwardWriter(outFileUrl);
        try {
            arbWriter.generateAwardDoc(ab);
        } catch (Exception e){
            e.printStackTrace();
            return ApiResult.resultWith(ResultCodeEnum.SYSTEM_DEAL_FAIL);
        }

        return CommonUtil.download(outPath, outFileName, outFileName);

    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
