<template>
  <router-view />
  <AiChatWidget v-if="showAiWidget" />
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AiChatWidget from './components/AiChatWidget.vue'

export default {
  name: 'App',
  components: { AiChatWidget },
  setup() {
    const route = useRoute()
    const showAiWidget = computed(() => {
      const hidden = ['Login', 'Register']
      return !hidden.includes(route.name)
    })
    return { showAiWidget }
  }
}
</script>

<style>
@import './assets/theme.css';
</style>
