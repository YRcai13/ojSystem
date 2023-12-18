package com.cyr.ojbackenduserservice.controller.inner;

import com.cyr.ojbackendserviceclient.service.UserFeignClient;
import com.cyr.ojbackendmodel.model.entity.User;
import com.cyr.ojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @author caiyu
 * @version 1.0
 * @description TODO
 * @date 2023/12/17 20:00
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

	@Resource
	private UserService userService;
	/**
	 * @description 根据 id 获取用户
	 * @param userId
	 * @return com.cyr.ojbackendmodel.model.entity.User
	 * @author
	 * @date 2023/12/17 19:24
	 */
	@Override
	@GetMapping("/get/id")
	public User getById(@RequestParam("id") long userId) {
		return userService.getById(userId);
	}

	/**
	 * @description 根据 ids 获取用户列表
	 * @param idList
	 * @return java.util.List<com.cyr.ojbackendmodel.model.entity.User>
	 * @author
	 * @date 2023/12/17 19:27
	 */
	@Override
	@GetMapping("/get/ids")
	public List<User> listByIds(@RequestParam("idList") Collection<Long> idList) {
		return userService.listByIds(idList);
	}

}
