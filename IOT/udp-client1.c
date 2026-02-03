#include "common.h"
#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define UDP_CLIENT_PORT	8765
#define UDP_SERVER_PORT	5678

static struct simple_udp_connection udp_conn;

#define SEND_INTERVAL		  (20 * CLOCK_SECOND)

static int state = 0;

static struct simple_udp_connection udp_conn;

/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
         const uip_ipaddr_t *sender_addr,
         uint16_t sender_port,
         const uip_ipaddr_t *receiver_addr,
         uint16_t receiver_port,
         const uint8_t *data,
         uint16_t datalen)
{
  message_t rcv_msg = *(message_t *)data;
  message_t snd_msg = {.type = NO_OP, .val = 0};

  LOG_INFO("Received msg type %d val %d from ", rcv_msg.type, rcv_msg.val);
  LOG_INFO_6ADDR(sender_addr);
  LOG_INFO("\n");

  switch(state) {
    case 0:
      if (rcv_msg.type != OK) {
        return;
      }

      snd_msg.type = WRITE;
      snd_msg.val = random_rand() % 1000;
      LOG_INFO("Sending write request to ");
      LOG_INFO_6ADDR(sender_addr);
      LOG_INFO_("val %d\n", snd_msg.val);
      state = 1;
      break;
    case 1:
      if (rcv_msg.type != OK) {
        state = 0;
        return;
      }

      snd_msg.type = READ;
      LOG_INFO("Sending read request to ");
      LOG_INFO_6ADDR(sender_addr);
      LOG_INFO_("\n");
      state = 2;
      break;
    case 2:
      if (rcv_msg.type != OK) {
        state = 0;
        return;
      }

      LOG_INFO("Value read %d\n", rcv_msg.val);
      state = 0;
      return;
    default:
      LOG_INFO("Unknown state!\n");
      return;
  }

  simple_udp_sendto(&udp_conn, &snd_msg, sizeof(snd_msg), sender_addr);
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data)
{
  static struct etimer periodic_timer;
  uip_ipaddr_t dest_ipaddr;

  PROCESS_BEGIN();

  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                      UDP_SERVER_PORT, udp_rx_callback);

  etimer_set(&periodic_timer, random_rand() % SEND_INTERVAL);
  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));

    if(NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)) {
      if (state != 0) {
        LOG_INFO("Already in a state machine!\n");
        continue;
      }

      LOG_INFO("Sending lock request to ");
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO_("\n");
      message_t msg = {.type = LOCK, .val = 0};
      simple_udp_sendto(&udp_conn, &msg, sizeof(message_t), &dest_ipaddr);
    } else {
      LOG_INFO("Not reachable yet\n");
    }

    etimer_set(&periodic_timer, random_rand() % SEND_INTERVAL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
