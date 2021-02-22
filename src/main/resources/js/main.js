import Vue from "vue";
import App from 'pages/App.vue';
import router from 'router/router.js';
import 'bootstrap/dist/css/bootstrap.min.css'
import vSelect from 'vue-select'
import 'vue-select/dist/vue-select.css';
Vue.component('v-select', vSelect);

new Vue({
    el: '#app',
    router,
    render: a => a(App)
});