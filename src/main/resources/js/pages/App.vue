<template>
    <div class="h-100">
        <vue-header/>
        <div class="row h-75">
            <div class="col-2 border-top border-right border-secondary text-center">
                <span class="justify-content-center border-bottom border-secondary w-100">{{rep}}</span>
                <div class="sidenav mt-3">
                    <a @click="showOrHideReestr">Счет-реестр выполненных услуг</a>
                </div>
                <div class="sidenav mt-3">
                    <a @click="showOrhideCovid">За оказанные услуги, на выявление РНК вируса COVID-19 из биологического материала методом ПЦР</a>
                </div>
            </div>
            <div class="col-10 border-top border-secondary p-0">
                <div class="col-10 border-top border-secondary p-0">
                    <reestr v-if="isReestr"></reestr>
                </div>
                <div class="col-10 border-top border-secondary p-0">
                    <covid v-if="isCovid"></covid>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import VueHeader from "../components/vue-header.vue";
    import Reestr from "../components/formreports/Reest.vue";
    import Covid from "../components/formreports/Covid.vue";
    export default  {
        name: 'App',
        components: {Reestr,Covid, VueHeader},
        data () {
            return {
                rep: "Отчеты",
                isReestr: false,
                isCovid: false
            }
        },
        methods: {
            cleanAll() {
                this.isCovid = false;
                this.isReestr = false;
            },
            showOrhideCovid() {
                this.cleanAll();
                this.isCovid = !this.isCovid;
            },
            showOrHideReestr() {
                this.cleanAll();
                this.isReestr = !this.isReestr;
            }
        }
    }
</script>

<style>
    .sidenav:hover {
        background: lightgrey;
        border-radius: 5px;
        cursor: pointer;
    }
</style>