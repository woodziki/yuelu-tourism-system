<template>
  <div class="ai-assistant">
    <el-button
      class="assistant-trigger"
      type="primary"
      circle
      icon="el-icon-chat-dot-round"
      @click="visible = true"
    />

    <el-drawer
      title="AI 智能导游"
      :visible.sync="visible"
      direction="rtl"
      size="360px"
      :with-header="true"
    >
      <div class="chat-body">
        <div class="messages" ref="msgList">
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="msg-row"
            :class="msg.role"
          >
            <div class="bubble">{{ msg.content }}</div>
          </div>
          <div v-if="sending" class="msg-row assistant">
            <div class="bubble">正在思考中...</div>
          </div>
        </div>
        <div class="input-box">
          <el-input
            v-model="input"
            type="textarea"
            :rows="3"
            placeholder="问我：怎么安排岳麓山半日游？"
            maxlength="300"
            show-word-limit
            @keyup.enter.native="onEnterSend"
          />
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!input.trim()"
            @click="send"
            style="margin-top: 8px; width: 100%;"
          >
            发送
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AiAssistant',
  data () {
    return {
      visible: false,
      input: '',
      sending: false,
      messages: [
        { role: 'assistant', content: '你好，我是岳麓山 AI 导游助手。你可以问我路线规划、打卡建议和避峰时段。' }
      ]
    }
  },
  methods: {
    onEnterSend (e) {
      if (e.shiftKey) return
      e.preventDefault()
      this.send()
    },
    send () {
      const text = this.input.trim()
      if (!text || this.sending) return
      this.messages.push({ role: 'user', content: text })
      this.input = ''
      this.sending = true
      this.scrollToBottom()

      request({
        url: '/ai/chat',
        method: 'post',
        data: { message: text },
        timeout: 60000
      })
        .then(res => {
          const answer = (res && res.answer) ? res.answer : '抱歉，我暂时没有组织好答案。'
          this.messages.push({ role: 'assistant', content: answer })
        })
        .catch(() => {
          this.messages.push({ role: 'assistant', content: '网络有点拥堵，请稍后再试。' })
        })
        .finally(() => {
          this.sending = false
          this.scrollToBottom()
        })
    },
    scrollToBottom () {
      this.$nextTick(() => {
        const el = this.$refs.msgList
        if (el) el.scrollTop = el.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.assistant-trigger {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 52px;
  height: 52px;
  z-index: 3000;
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.35);
}

.chat-body {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 90px);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 4px 2px 12px;
}

.msg-row {
  display: flex;
  margin-bottom: 10px;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 84%;
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.msg-row.user .bubble {
  background: #409eff;
  color: #fff;
}

.msg-row.assistant .bubble {
  background: #f2f6fc;
  color: #303133;
}

.input-box {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}
</style>
