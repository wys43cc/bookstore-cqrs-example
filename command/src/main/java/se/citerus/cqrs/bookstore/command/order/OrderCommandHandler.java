package se.citerus.cqrs.bookstore.command.order;

import com.google.common.eventbus.Subscribe;
import se.citerus.cqrs.bookstore.command.CommandHandler;
import se.citerus.cqrs.bookstore.domain.Repository;
import se.citerus.cqrs.bookstore.domain.order.Order;
import se.citerus.cqrs.bookstore.domain.order.OrderLines;
import se.citerus.cqrs.bookstore.order.OrderLine;
import se.citerus.cqrs.bookstore.publisher.PublisherId;
import se.citerus.cqrs.bookstore.query.QueryService;

import java.util.ArrayList;
import java.util.List;

public class OrderCommandHandler implements CommandHandler {

  private final Repository repository;
  private final QueryService queryService;

  public OrderCommandHandler(Repository repository, QueryService queryService) {
    this.repository = repository;
    this.queryService = queryService;
  }

  @Subscribe
  public void handle(PlaceOrderCommand command) {
    Order order = new Order();

    List<OrderLine> orderLinesWithPublishers = new ArrayList<>();
    for (OrderLine orderLine : command.orderLines) {
      PublisherId publisherId = queryService.findPublisher(orderLine.bookId);
      orderLinesWithPublishers.add(orderLine.withPublisher(publisherId));
    }
    order.place(command.orderId, command.customerInformation, new OrderLines(orderLinesWithPublishers));
    repository.save(order);
  }

  @Subscribe
  public void handle(ActivateOrderCommand command) {
    Order order = repository.load(command.orderId, Order.class);
    order.activate();
    repository.save(order);
  }

}