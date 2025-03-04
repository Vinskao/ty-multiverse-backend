package com.mli.dashboard.modules.ckeditor.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EditorContentMapper {

    /**
     * 根據 editor 查詢儲存的內容
     * 
     * @param editor 編輯器名稱
     * @return 對應的內容
     */
    @Select("SELECT content FROM ckeditor WHERE editor = #{editor}")
    String findContentByEditor(@Param("editor") String editor);

    /**
     * 插入或更新 editor 的內容
     * 
     * @param editor  編輯器名稱
     * @param content 儲存的內容
     * @return 影響的行數
     */
    @Insert("""
            INSERT INTO ckeditor (editor, content)
            VALUES (#{editor}, #{content})
            ON CONFLICT (editor)
            DO UPDATE SET content = EXCLUDED.content
            """)
    int upsertContent(@Param("editor") String editor, @Param("content") String content);
}