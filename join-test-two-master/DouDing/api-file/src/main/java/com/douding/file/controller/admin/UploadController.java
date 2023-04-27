package com.douding.file.controller.admin;


import com.douding.server.dto.FileDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.service.FileService;
import com.douding.server.service.TestService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/*
    返回json 应用@RestController
    返回页面  用用@Controller
 */
@RequestMapping("/admin/file")
@RestController
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    public  static final String BUSINESS_NAME ="文件上传";
    @Resource
    private TestService testService;

    @Value("${file.path}")
    private String FILE_PATH;

    @Value("${file.domain}")
    private String FILE_DOMAIN;

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseDto upload(@RequestBody FileDto fileDto) throws Exception {
        String key = fileDto.getKey();
        Integer shardIndex = fileDto.getShardIndex();
        byte[] bytes = Base64.decodeBase64(fileDto.getShard().replace("data:application/octet-stream;base64,", ""));

        String prefix = ResourceUtils.getURL("classpath:").getPath() + FILE_PATH;
        prefix += key.charAt(0) + "/" + key.charAt(1) + "/chunk/";
        File dir = new File(prefix);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ResponseDto<Object> responseDto = new ResponseDto<>();

        File file = new File(prefix, shardIndex.toString());
        boolean newFile = file.createNewFile();
        if (newFile) {
            try (
                    RandomAccessFile write = new RandomAccessFile(file, "rw");
            ) {
                write.write(bytes);
            }
        }

        if (Objects.equals(fileDto.getShardIndex(), fileDto.getShardTotal())) {
            merge(fileDto);
            FileDto dto = new FileDto();
            dto.setPath(FILE_DOMAIN + key.charAt(0) + "/" + key.charAt(1) + "/" + key + "." + fileDto.getSuffix());
            responseDto.setContent(dto);
        }
        return responseDto;
    }

    //合并分片
    public void merge(FileDto fileDto) throws Exception {
        LOG.info("合并分片开始");

        String key = fileDto.getKey();
        String prefix = ResourceUtils.getURL("classpath:").getPath() + FILE_PATH;
        prefix += key.charAt(0) + "/" + key.charAt(1) + "/";

        File chunkFolderPath = new File(prefix + "chunk/");

        File mergeFile = new File(prefix, key + "." + fileDto.getSuffix());
        mergeFile.createNewFile();

        List<File> files = Arrays.stream(chunkFolderPath.listFiles())
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        try (
                RandomAccessFile write = new RandomAccessFile(mergeFile, "rw")
        ) {
            byte[] bytes = new byte[1024];
            for (File file : files) {
                try (
                        RandomAccessFile read = new RandomAccessFile(file, "r");
                ) {
                    int len;
                    while ((len = read.read(bytes)) != -1) {
                        write.write(bytes, 0, len);
                    }
                }
            }
        }
    }


         @GetMapping("/check/{key}")
        public ResponseDto<FileDto> check (@PathVariable String key) throws Exception {
            LOG.info("检查上传分片开始：{}", key);
            ResponseDto<FileDto> responseDto = new ResponseDto<>();
            String filePath = FILE_PATH + key;
            int chunk = 1;
            // 判断第一分批是否存在
            String fileChuck = filePath + "-" + chunk;
            File file = new File(fileChuck);
            // 不存在，直接返回
            if (!file.exists()) {
                responseDto.setSuccess(true);
                return responseDto;
            }
            // 判断后续分块是否存在
            chunk++;
            while (true) {
                fileChuck = filePath + "-" + chunk;
                file = new File(fileChuck);
                if (!file.exists()) {
                    break;
                }
                chunk++;
            }
            // 返回最后一块分块号
            FileDto fileDto = new FileDto();
            fileDto.setShardIndex(chunk - 1);
            responseDto.setSuccess(true);
            responseDto.setContent(fileDto);
            return responseDto;
        }


}//end class
