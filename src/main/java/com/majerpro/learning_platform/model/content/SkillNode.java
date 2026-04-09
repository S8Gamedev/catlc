package com.majerpro.learning_platform.model.content;

import com.majerpro.learning_platform.model.Skill;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_node")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SkillNode parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nodeOrder ASC")
    private List<SkillNode> children = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String objective;

    @Column(name = "node_order")
    private Integer nodeOrder;

    @Column(nullable = false)
    private Integer depth;
}