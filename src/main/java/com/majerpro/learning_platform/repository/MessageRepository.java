package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Message;
import com.majerpro.learning_platform.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
