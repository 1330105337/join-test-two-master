package com.douding.business.controller.admin;




import com.douding.server.dto.CategoryDto;
import com.douding.server.dto.TeacherDto;
import com.douding.server.dto.PageDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.service.TeacherService;
import com.douding.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/admin/teacher")
public class TeacherController {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherController.class);
    //给了日志用的
    public  static final String BUSINESS_NAME ="讲师";

    @Resource
    private TeacherService teacherService;

    @RequestMapping("/list")
    public ResponseDto list(PageDto pageDto){
        ResponseDto<PageDto> responseDto = new ResponseDto<>();
        teacherService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/save")
    public ResponseDto save(@RequestBody TeacherDto teacherDto){
        ValidatorUtil.require(teacherDto.getImage(), "教师头像");
        ValidatorUtil.require(teacherDto.getName(), "教师名");
        ValidatorUtil.require(teacherDto.getPosition(), "教师职位");
        ValidatorUtil.require(teacherDto.getIntro(),"教师简介");
        ValidatorUtil.require(teacherDto.getNickname(),"教师昵称");
        ValidatorUtil.require(teacherDto.getMotto(),"教师座右铭");
        teacherService.save(teacherDto);
        ResponseDto<TeacherDto> responseDto = new ResponseDto<>();
        responseDto.setContent(teacherDto);
        return responseDto;
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable String id){
        ResponseDto<TeacherDto> responseDto = new ResponseDto<>();
        teacherService.delete(id);
        return responseDto;
    }

    @RequestMapping("/all")
    public ResponseDto all(){
        ResponseDto responseDto = new ResponseDto();
        List<TeacherDto> teacherDtoList = teacherService.all();
        responseDto.setContent(teacherDtoList);
        return responseDto;
    }

}//end class