package cn.bugstack.ai.domain.agent.service.armory.matter.mcp.client.impl;

import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.service.armory.matter.mcp.client.ToolMcpCreateService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Service
public class StreamableHttpToolMcpCreateService implements ToolMcpCreateService {
    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StreamableHttpParameters streamablehttp = toolMcp.getStreamablehttp();

        String url = streamablehttp.getUrl();
        Long requestTimeout = streamablehttp.getRequestTimeout();
        Map<String, String> headers = streamablehttp.getHeaders();

        if (StringUtils.isBlank(url)) {
            throw new RuntimeException("tool mcp streamablehttp url is null!");
        }


        HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(url).endpoint("/");

        if(headers != null && !headers.isEmpty()){
            builder.httpRequestCustomizer((requestBuilder,method,endpoint,body,context)->{
                headers.forEach(requestBuilder::header);

                // 打印请求详情
                log.info("MCP Request - Method: {}, URL: {}", method, endpoint);
                log.info("Headers: {}", requestBuilder.build().headers().map());
            });
        }

        HttpClientStreamableHttpTransport streamableHttpTransport = builder.build();

        McpSyncClient mcpSyncClient = McpClient.sync(streamableHttpTransport).requestTimeout(Duration.ofMillis(requestTimeout == null ? 30000L : requestTimeout)).build();

        McpSchema.InitializeResult initialize = mcpSyncClient.initialize();

        return SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClient).build()
                .getToolCallbacks();
    }
}
