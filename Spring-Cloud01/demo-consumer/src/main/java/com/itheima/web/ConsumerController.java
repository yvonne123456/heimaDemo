package com.itheima.web;


import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("consumer")
public class ConsumerController {


    @Autowired
    private RestTemplate restTemplate;


//    @Autowired
//    private DiscoveryClient discoveryClient;

    /**
     *
     * @param id
     * @return user
     */
//
//    @GetMapping("{id}")
//    public User queryById(@PathVariable("id") Long id){
//        List<ServiceInstance> instances = discoveryClient.getInstances("provider");
//
//        ServiceInstance serviceInstance = instances.get(0);
//
//        String url = String.format("http://%s:%s/user/%s", serviceInstance.getHost(),serviceInstance.getPort(), id);
//
//        User user = restTemplate.getForObject("url", User.class);
//        return user;



    @GetMapping("{id}")
    public User queryById(@PathVariable("id") Long id){
        String url = "http://localhost:8081/user/" + id;
        // 调用RestTemplate的getForObject方法，指定url地址和返回值类型
        User user = restTemplate.getForObject(url,User.class);
        return user;
    }


}
