package com.lucamusic.order.service;

import com.lucamusic.order.model.Event;
import com.lucamusic.order.model.EventResponse;
import com.lucamusic.order.model.Order;
import com.lucamusic.order.model.OrderInfo;
import com.lucamusic.order.model.User;
import com.lucamusic.order.model.UserResponse;
import com.lucamusic.order.model.PaymentResponse;
import com.lucamusic.order.model.PaymentInfo;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * Creates an order
	 * @param info
	 * @return
	 */
	@SneakyThrows
	@Override
	public Order createOrder(String eventId, String userId, OrderInfo info, String extractToken) {
		
		HttpHeaders headers= new HttpHeaders();
		headers.add("Authorization", "Bearer " + extractToken);
		HttpEntity<String> request = new HttpEntity<String>(headers);
		System.out.println(request);
	
		final ResponseEntity<UserResponse> user = restTemplate.exchange("http://user-service/users/" + userId, HttpMethod.GET, request, UserResponse.class);	
		user.getBody();
		
		final EventResponse event = restTemplate.getForObject("http://event-service/events/" + eventId, EventResponse.class);
		
		

		
		Order order = Order.builder()
				.eventName(event.getName())
				.musicStyle(event.getMusicStyle())
				.userName(user.getBody().getFullName())
				.numTickets(info.getNumTickets())
				.build();

		
		
		String operationStatus = validateOrder(info.getPaymentInfo()).getStatus();
		if(operationStatus.equals("Valid account")){
			order.setStatus("Pago aceptado");
		} else
			order.setStatus(operationStatus);
		
		return order;
		
		
	}

	/**
	 * Request a validation to an external payment service
	 * @param paymentInfo Payment information
	 * @return Response
	 */
	@Override
	public PaymentResponse validateOrder(PaymentInfo paymentInfo) {
		
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject("http://localhost:8050/" , paymentInfo,PaymentResponse.class);
		
		
				
	}
}
