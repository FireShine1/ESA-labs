package ru.fireshine.laba4.controller;

import java.util.List;

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
import ru.fireshine.laba4.service.DishService;
import ru.fireshine.laba4.service.UtilsService;

@Controller
public class DishesController {
	
	@Autowired
	private DishService dishService;
	@Autowired
	private UtilsService utils;
	@Autowired
	private JmsPublisherService jmsPublisherService;
	
	@RequestMapping("/")
	public String startRedirect() {
		return "redirect:/dishes";
	}
	
	@RequestMapping("/dishes")
	public String viewDishes(Model m) {
		List<Dish> dishes = dishService.findAll();
		m.addAttribute("dishes", dishes);
		return "dishes";
	}
	
	@RequestMapping(value = "/addDish", method = RequestMethod.GET)
	public String dishForm(Model m, @RequestParam(required = false) Long edit) {
		if (edit == null) {
			m.addAttribute("dish", new Dish());
		} else {
			m.addAttribute("dish", dishService.findById(edit));
		}
		return "addDish";
	}
	
	@RequestMapping(value = "/addDish", method = RequestMethod.POST)
	public String saveDish(@ModelAttribute("dish") Dish dish) throws JMSException {
		if (dish.getId() == 0) {
			dishService.insert(dish);
			jmsPublisherService.sendEvent(Dish.class, dish, EventType.CREATE);
		} else {
			dishService.update(dish);
			jmsPublisherService.sendEvent(Dish.class, dish, EventType.UPDATE);
		}
		return "redirect:/dishes";
	}
	
	@RequestMapping(value = "/delDish", method = RequestMethod.GET)
	public String delDish(Model m, @RequestParam Long id) throws JMSException {
		Dish dish = dishService.findById(id);
		dishService.delete(id);
		jmsPublisherService.sendEvent(Dish.class, dish, EventType.DELETE);
		return "redirect:/dishes";
	}
	
	@RequestMapping(value = "/fillTables", method = RequestMethod.GET)
	public String fillTables() {
		utils.fillTables();
		return "redirect:/dishes";
	}
	
}