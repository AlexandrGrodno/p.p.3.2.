package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
//@RestController
@Controller

public class AdminController {
    private UserService userService;
    private RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "/admin/addUser")
    public String addUser(Model model) {
        model.addAttribute("users", new User());
        model.addAttribute("role1", roleService.getListRole());
        return "editUser";
    }

    @GetMapping(value = "/admin/user/id")
    public String editUser(@RequestParam(value = "id", defaultValue = "0") int id, Model model) {
        if (id > 0) {
            model.addAttribute("users", userService.findUserById(id));
        }
        model.addAttribute("role1", roleService.getListRole());
        return "editUser";
    }
    @GetMapping(value = "/admin/user/{id}")
    public ResponseEntity<User> edit(@PathVariable  int id) {
        User user= new User();
        user = userService.findUserById(id);

        return  new ResponseEntity<>(user, HttpStatus.OK);
    }
    @PatchMapping("/admin/user")
    public ResponseEntity<HttpStatus> updateUser(@RequestBody User user){

        Set<Role> role2 = user.getRoles().stream().map(x->x.getRole().toString())
                .map(roleService::findRoleByName)
                .collect(Collectors.toSet());

        user.setRoles(role2);
        userService.saveUser(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/admin/user")
    public String saveUser(@Validated @ModelAttribute("users") User user, BindingResult bindingResult,
                           @RequestParam(value = "roles", defaultValue = "ROLE_USER") List<String> roleNames, Model model) {

        if (bindingResult.hasFieldErrors("username")
                || (bindingResult.hasFieldErrors("lastnName"))
                || (bindingResult.hasFieldErrors("password"))
                || (bindingResult.hasFieldErrors("age"))) {
            model.addAttribute("role1", roleService.getListRole());
            return "editUser";
        }
        Set<Role> role2 = roleNames.stream()
                .map(roleService::findRoleByName)
                .collect(Collectors.toSet());

        user.setRoles(role2);
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping(value = "/admin")
    public String adminka(Model model,  Principal userDetails) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUser",userService.findByUsername(userDetails.getName()).get());
        model.addAttribute("roles", roleService.getListRole());
        System.out.println(userService.findByUsername(userDetails.getName()).get());
        return "adminpanel";
    }

    @DeleteMapping(value = "/admin/user/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable int id) {
        userService.deleteUserById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
