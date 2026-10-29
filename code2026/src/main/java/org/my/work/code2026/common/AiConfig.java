package org.my.work.code2026.common;

import org.my.work.code2026.service.AiTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory
                .builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AiTools aiTools) {
        return builder
                .defaultSystem("""
                        你是校园小卖部的智能AI助手，名叫小米。
                        你可以帮用户查询商品信息。
                        当查询商品时，必须调用工具获取真实数据，禁止编造。

                        【重要格式要求】
                        工具返回结果中，商品名带有 [商品:名称|id:数字] 这样的标记。
                        你在回复时必须完整保留这个标记，一个字都不要改、不要删、不要加空格、不要换格式。
                        例如：有的，我们有以下辣条：[商品:卫龙辣条|id:5]
                             价格：3
                             销量：100。

                        【输出要求】
                        每个商品只展示：商品名、价格、销量。
                        不要额外添加库存、简介、描述等工具没有提供的信息。
                        回答简洁口语化，不要自己加"元"、"件"等多余单位。
                        """)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultTools(aiTools)
                .build();
    }
}