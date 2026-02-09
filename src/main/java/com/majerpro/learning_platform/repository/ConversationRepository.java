package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Conversation;
import com.majerpro.learning_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByLastMessageAtDesc(User user);
}
