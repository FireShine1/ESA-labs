package ru.fireshine.laba4.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ru.fireshine.laba4.messages.JmsPublisherService;
import ru.fireshine.laba4.model.Dish;
import ru.fireshine.laba4.model.EventType;
import ru.fireshine.laba4.model.Restaurant;
import ru.fireshine.laba4.service.DishService;
import ru.fireshine.laba4.service.RestaurantService;

@Controller
public class RestaurantsController {
	
	@Autowired
	private RestaurantService restaurantService;
	@Autowired
	private DishService dishService;
	@Autowired
	private JmsPublisherService jmsPublisherService;
	
	@RequestMapping(value="/restaurants",method = RequestMethod.GET)
	public String viewRestaurants(Model m) {
		List<Restaurant> restaurants = restaurantService.findAll();
		m.addAttribute("restaurants", restaurants);
		return "restaurants";
	}
	
	@RequestMapping(value = "/addRes", method = RequestMethod.GET)
	public String restaurantForm(Model m, @RequestParam(required = false) Long edit) {
		m.addAttribute("dishes", dishService.findAll());
		if (edit == null) {
			m.addAttribute("restaurant", new Restaurant());
		} else {
			m.addAttribute("restaurant", restaurantService.findById(edit));
		}
		return "addRes";
	}
	
	@RequestMapping(value = "/addRes", method = RequestMethod.POST)
	public String saveRestaurant(@ModelAttribute("restaurant") Restaurant restaurant) throws JMSException {
		Set<Dish> dishes = new HashSet<Dish>();
		restaurant.getDishesId().forEach(id -> {
			dishes.add(dishService.findById(id));
		});
		restaurant.setDishes(dishes);
		if (restaurant.getId() == 0) {
			restaurantService.insert(restaurant);
			jmsPublisherService.sendEvent(Restaurant.class, restaurant, EventType.CREATE);
		} else {
			restaurantService.update(restaurant);
			jmsPublisherService.sendEvent(Restaurant.class, restaurant, EventType.UPDATE);
		}
		return "redirect:/restaurants";
	}
	
	@RequestMapping(value = "/delRes", method = RequestMethod.GET)
	public String delRestaurant(Model m, @RequestParam Long id) throws JMSException {
		Restaurant restaurant = restaurantService.findById(id);
		restaurantService.delete(id);
		jmsPublisherService.sendEvent(Restaurant.class, restaurant, EventType.DELETE);
		jmsPublisherService.sendRestaurantDeleted(restaurant, EventType.DELETE);
		return "redirect:/restaurants";
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String search(Model m, @RequestParam(required = false) String cookery, @RequestParam(required = false) String dishesToFind) {
		String dishes[] = dishesToFind.split(",");
		List<String> dishesList = new ArrayList<>();
		Collections.addAll(dishesList, dishes);
		m.addAttribute("restaurants", restaurantService.findAll(cookery, dishesList));
		return "restaurants";
	}
	
}