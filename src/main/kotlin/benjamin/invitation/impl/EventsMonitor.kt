package benjamin.invitation.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
class EventsMonitor(
    private val countDownLatch: CountDownLatch,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    invitationEventRepository: InvitationEventRepository,
) {
    private val invitationEventService = InvitationEventService(invitationEventRepository)
    private val topic = "BENJAMIN.EMAIL"
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper().apply {
        findAndRegisterModules()
    }

    @Scheduled(fixedDelay = 3000)
    fun sendEvents() {
        invitationEventService.getAll()
            .map { Pair(ProducerRecord(topic, null, it.eventId.toString(), mapper.writeValueAsString(it)), it.eventId) }
            .forEach {
                kafkaTemplate.send(it.first).get()
                logger.info("${it.second} is sent to Kafka")
                invitationEventService.deleteById(it.second)
            }
        countDownLatch.countDown()
    }
}