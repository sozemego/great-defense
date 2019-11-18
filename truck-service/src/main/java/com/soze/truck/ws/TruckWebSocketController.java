package com.soze.truck.ws;

import com.soze.common.json.JsonUtils;
import com.soze.common.message.client.BuyResourceRequest;
import com.soze.common.message.client.ClientMessage;
import com.soze.common.message.client.DumpContent;
import com.soze.common.message.client.SellResourceRequest;
import com.soze.common.message.client.TruckTravelRequest;
import com.soze.common.message.server.TruckAdded;
import com.soze.truck.domain.Player;
import com.soze.truck.domain.Truck;
import com.soze.truck.repository.PlayerRepository;
import com.soze.truck.service.SessionRegistry;
import com.soze.truck.service.TruckConverter;
import com.soze.truck.service.TruckService;
import com.soze.truck.service.TruckServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Component
public class TruckWebSocketController extends TextWebSocketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(TruckWebSocketController.class);

	private final SessionRegistry sessionRegistry;
	private final TruckService truckService;
	private final TruckConverter truckConverter;
	private final PlayerRepository playerRepository;
	private final TruckServiceStarter truckServiceStarter;

	@Autowired
	public TruckWebSocketController(SessionRegistry sessionRegistry, TruckService truckService,
																	TruckConverter truckConverter, PlayerRepository playerRepository,
																	TruckServiceStarter truckServiceStarter
																 ) {
		this.sessionRegistry = sessionRegistry;
		this.truckService = truckService;
		this.truckConverter = truckConverter;
		this.playerRepository = playerRepository;
		this.truckServiceStarter = truckServiceStarter;
	}


	@Override
	public void afterConnectionEstablished(WebSocketSession session
																				) throws Exception {
		LOG.info("{} connected", session.getId());
		sessionRegistry.addSession(session);
		String playerName = session.getPrincipal().getName();
		Player player = playerRepository.findByNameEquals(playerName).get();
		truckServiceStarter.startPlayer(player.getId());
		for (Truck truck : truckService.getTrucks(player.getId())) {
			sessionRegistry.sendTo(session, new TruckAdded(truckConverter.convert(truck)));
		}
	}


	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status
																	 ) throws Exception {
		LOG.info("{} disconnected", session.getId());
		sessionRegistry.removeSession(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		LOG.info("TransportationError", exception);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message
																	) throws Exception {
		try {
			LOG.trace("Received message from client id {}", session.getId());
			ClientMessage clientMessage = JsonUtils.parse(message.getPayload(), ClientMessage.class);
			LOG.trace("Client message type = {} from client id = {}", clientMessage.getType(), session.getId());

			if (clientMessage.getType() == ClientMessage.ClientMessageType.TRUCK_TRAVEL_REQUEST) {
				TruckTravelRequest truckTravelRequest = (TruckTravelRequest) clientMessage;
				truckService.travel(
					UUID.fromString(truckTravelRequest.getTruckId()), truckTravelRequest.getDestinationCityId());
			}

			if (clientMessage.getType() == ClientMessage.ClientMessageType.BUY_RESOURCE_REQUEST) {
				BuyResourceRequest buyResourceRequest = (BuyResourceRequest) clientMessage;
				truckService.buyResource(UUID.fromString(buyResourceRequest.getTruckId()), buyResourceRequest.getFactoryId(),
																 buyResourceRequest.getResource(), buyResourceRequest.getCount()
																);
			}
			if (clientMessage.getType() == ClientMessage.ClientMessageType.SELL_RESOURCE_REQUEST) {
				SellResourceRequest sellResourceRequest = (SellResourceRequest) clientMessage;
				truckService.sellResource(UUID.fromString(sellResourceRequest.getTruckId()), sellResourceRequest.getFactoryId(),
																	sellResourceRequest.getResource(), sellResourceRequest.getCount()
																 );
			}

			if (clientMessage.getType() == ClientMessage.ClientMessageType.DUMP_CONTENT) {
				DumpContent dumpContent = (DumpContent) clientMessage;
				truckService.dump(UUID.fromString(dumpContent.getEntityId()));
			}
		} catch (Exception e) {
			LOG.info("Exception during message handling", e);
		}

	}
}
