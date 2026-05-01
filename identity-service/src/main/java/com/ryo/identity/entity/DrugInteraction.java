package com.ryo.identity.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tuong_tac_thuoc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "muc_do_nghiem_trong")
    private String mucDoNghiemTrong;

    @Column(name = "hau_qua_cua_tuong_tac", columnDefinition = "TEXT")
    private String hauQuaCuaTuongTac;

    @Column(name = "co_che_tuong_tac", columnDefinition = "TEXT")
    private String coCheTuongTac;

    @Column(name = "xu_tri_tuong_tac", columnDefinition = "TEXT")
    private String xuTriTuongTac;

    // Tên text lưu trữ (đề phòng trường hợp không join bảng)
    @Column(name = "hoat_chat_1")
    private String hoatChat1Name;

    @Column(name = "hoat_chat_2")
    private String hoatChat2Name;

    // Khóa ngoại tới MergedIngredient (Hoạt chất 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoat_chat_1_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private MergedIngredient ingredient1;

    // Khóa ngoại tới MergedIngredient (Hoạt chất 2)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoat_chat_2_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private MergedIngredient ingredient2;
}