
package com.example.mywebapp;

import com.example.mywebapp.repository.AddressRepository;
import com.example.mywebapp.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final UserService userService;
    private final AddressRepository addressRepository;

    public HomeController(
            UserService userService,
            AddressRepository addressRepository) {

        this.userService = userService;
        this.addressRepository = addressRepository;
    }

    
@GetMapping("/")
public String home(Model model) {

    List<User> users = userService.getAllUsers();
    List<Address> allAddresses = addressRepository.findAll();

    Map<Long, List<Address>> addressesByUserId = new HashMap<>();

    for (User user : users) {
        addressesByUserId.put(
            user.getId(),
            new ArrayList<>()
        );
    }

    for (Address address : allAddresses) {
        Long userId = address.getUser().getId();

        addressesByUserId
            .computeIfAbsent(userId, id -> new ArrayList<>())
            .add(address);
    }

    model.addAttribute("users", users);
    model.addAttribute("addressesByUserId", addressesByUserId);

    return "home";
}


    @GetMapping("/users/new")
    public String showAddForm(Model model) {

        model.addAttribute("user", new User());
        model.addAttribute("formTitle", "Add User");

        return "user-form";
    }


    

    @PostMapping("/users/save")
    public String saveUser(
            @ModelAttribute("user") User user,
            @RequestParam("city") String city,
            @RequestParam("state") String state) {

        // Save the user, including the phone number
        user = userService.saveUser(user);

        // Find the existing address or create one if none exists
        Address address = addressRepository.findByUserId(user.getId())
                .orElse(new Address());

        // Connect the address to the user
        address.setUser(user);
        address.setCity(city);
        address.setState(state);

        // Save or update the address
        addressRepository.save(address);

        return "redirect:/";
    }


    
    @GetMapping("/users/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        User user = userService.getUserById(id);

        model.addAttribute("user", user);
        model.addAttribute("formTitle", "Edit User");

        Address address = addressRepository.findByUserId(id)
                .orElse(new Address());

        model.addAttribute("address", address);

        return "user-form";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {

        // Step 1: Find addresses belonging to this user
        List<Address> addresses = addressRepository.findAll()
                .stream()
                .filter(address ->
                        address.getUser().getId().equals(id))
                .toList();

        // Step 2: Delete those addresses first
        addressRepository.deleteAll(addresses);

        // Step 3: Delete the user
        userService.deleteUser(id);

        return "redirect:/";
    }
}
