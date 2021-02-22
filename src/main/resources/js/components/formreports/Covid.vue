<template>
    <div class="w-75 h-100 mx-auto pt-4">
        <div class="w-100 h-75 border-top border-secondary">
            <div class="row pt-4 pb-4 justify-content-center">
                <input class="mr-1" type="date" v-model="startDate">
                <span> - </span>
                <input class="ml-1" type="date" v-model="endDate">
            </div>
            <div>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <input type="checkbox" id="excel-radio" v-model="isExcel" :checked="isExcel" @change="isPdf = !isPdf">
                            <label for="excel-radio">Excel</label>
                            <input type="checkbox" id="pdf-radio" v-model="isPdf" style="margin-left: 15px" @change="isExcel = !isExcel">
                            <label for="pdf-radio">PDF</label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="w-100 pt-4 pb-4 justify-content-center">
                <label for="dates-container">Выбирать по местонахождению</label>
                <div id="dates-container">
                    <div class="form-check-inline">
                        <label class="form-check-label custom-radio">
                            <input type="radio" class="form-check-input" @change="ispChange" name="optradio" checked>исполнителя(орг-ции, проводившей исследования)
                        </label>
                    </div>
                </div>
            </div>

            <div class="row w-100">
                <div class="col-3">
                    <div class="custom-control-inline custom-radio ml-4">
                        <input type="radio" class="custom-control-input" id="defaultChecked" name="ff" @change="onDefault" checked>
                        <label class="custom-control-label" for="defaultChecked">Все данные</label>
                    </div>
                </div>
                <div class="col-9">
                </div>
            </div>
            <div class="row w-100">
                <div class="col-3">
                    <div class="custom-control-inline custom-radio ml-4">
                        <input type="radio" class="custom-control-input" id="defaultUnchecked1" name="ff" @change="onOblChange">
                        <label class="custom-control-label" for="defaultUnchecked1">{{inpOblName}}</label>
                    </div>
                </div>
                <div class="col-9">
                    <div class="w-100" v-if="isObl">
                        <label class="mr-sm-2" for="oblSelect">Выбор Области</label>
                        <select class="custom-select mb-2 mr-sm-2 mb-sm-0" id="oblSelect" v-model="state" @change="sortRegions">
                            <option selected>Выбор...</option>
                            <option v-for="(state, key) in states" v-bind:value="key">{{state}}</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="row w-100 mb-2">
                <div class="col-3">
                    <div class="custom-control-inline custom-radio ml-4">
                        <input type="radio" class="custom-control-input" id="defaultUnchecked2" name="ff" @change="onRegChange">
                        <label class="custom-control-label" for="defaultUnchecked2">{{inpRegName}}</label>
                    </div>
                </div>
                <div class="col-9">
                    <div class="w-100" v-if="isReg">
                        <label class="mr-sm-2" for="regSelect">Выбор Региона</label>
                        <select class="custom-select mb-2 mr-sm-2 mb-sm-0" id="regSelect" v-model="region">
                            <option selected>Выбор...</option>
                            <option v-for="(region, key) in sortedRegions" v-bind:value="key">{{region.value}}</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="row w-100 border-bottom border-secondary pb-2">
                <div class="w-100 text-center">
                    <button :disabled="isDisabled" type="button" class="btn btn-primary" @click="sendDataAndGetFile">{{message}}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        name: 'covid',
        data() {
            return {
                message: 'Сформировать',
                isDisabled: false,
                endDate: new Date().toISOString().substr(0, 10),
                startDdate: null,
                isIsp: true,
                inpOblName: "По области исп-ля",
                inpRegName: 'По району исп-ля',
                inpName: 'По исполнителю',
                isObl: false,
                isReg: false,
                isName: false,
                isAllUsers: true,
                lab: '',
                states: {},
                sortedRegions: {},
                regions: {},
                state: 'Выбор...',
                areas: "",
                region: 'Выбор...',
                objRazdelList: {},
                objPodRazdelList: {},
                sortedObjPodRazdelList: {},
                objList: {},
                sortedObjList: {},
                labList: {},
                fragText: '',
                objSection: '',
                objSubsection: '',
                objValue: '',
                options: [],
                test: '',
                isExcel: true,
                isPdf: false
            }
        },
        methods: {
            ispChange() {
                this.inpOblName = 'По области исп-ля';
                this.inpRegName = 'По району исп-ля';
                this.inpName = 'По исполнителю';
                this.isIsp = true;
            },
            onOblChange() {
                this.isReg = false;
                this.isName = false;
                this.isObl = true;
            },
            onRegChange() {
                this.isName = false;
                this.isObl = true;
                this.isReg = true;
            },
            onNameChange() {
                this.isObl = false;
                this.isReg = false;
                this.isName = true;
                this.options = this.sortForSelect();
            },
            onDefault() {
                this.isObl = false;
                this.isReg = false;
                this.isName = false;
                this.state = 'Выбор...';
                this.region = 'Выбор...';
            },
            sortComponent(from, to, field) {
                for (let key in to) {
                    delete to[key]
                }
                for (let key in from) {
                    if (parseInt(from[key].parentKey) === parseInt(field)) {
                        to[key] = from[key]
                    }
                }
            },
            sortRegions() {
                this.sortComponent(this.regions, this.sortedRegions, this.state);
            },
            sortPodRazdel() {
                this.sortComponent(this.objPodRazdelList, this.sortedObjPodRazdelList, this.objSection);

            },
            sortObjName() {
                this.sortComponent(this.objList, this.sortedObjList, this.objSubsection);
            },
            sendDataAndGetFile() {
                this.message = 'Подождите';
                this.isDisabled = true;
                axios({
                    url: 'generateReport/covid',
                    method: 'POST',
                    responseType: 'blob',
                    data: {
                        startDate: (this.startDdate) ? this.startDdate : this.startDate,
                        endDate: this.endDate,
                        isIsp: this.isIsp,
                        state: this.state,
                        stateName: this.states[this.state],
                        areaName: this.areas[this.state],
                        region: this.region,
                        regionName: this.regions[this.region],
                        isName: this.isName,
                        fragText: this.fragText,
                        objSection: this.objSection,
                        objSubsection: this.objSubsection,
                        objValue: this.objValue,
                        isAllUsers: this.isAllUsers,
                        lab: this.lab,
                        isExcel: this.isExcel,
                        isPdf: this.isPdf
                    }
                }).then((res) => {
                    const url = window.URL.createObjectURL(new Blob([res.data]));
                    const link = document.createElement('a');
                    link.href = url;
                    let fileName = "";
                    if (this.isExcel) {
                        fileName = 'file.xlsx';
                    } else {
                        fileName = 'file.pdf'
                    }
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                    this.isDisabled = false;
                    this.message = 'Сформировать';
                });
            },
            getComponentDataFromDict(obj) {
                let dictCode = obj.dictCode;
                let keyCode = obj.keyCode;
                let valueCode = obj.valueCode;
                let result = {};
                let baseUrl = 'http://lis.nce.kz/Synergy/rest/api/dictionary/get_by_code?dictionaryCode=' + dictCode;
                axios.get(baseUrl, {
                    auth: {
                        username: 'admincrm',
                        password: 'Adm1nCRM',
                    }
                }).then((res) => {
                    let data = res.data;
                    let keyId = '';
                    let valueId = '';
                    data.columns.forEach((value) => {
                        if (value.code === keyCode) {
                            keyId = value.columnID;
                        }
                        if (value.code === valueCode) {
                            valueId = value.columnID;
                        }
                    });
                    let items = data.items;
                    items.forEach((item) => {
                        let key;
                        let value;

                        item.values.forEach((val) => {
                            if (val.columnID === keyId) {
                                key = val.value;
                            }
                            if (val.columnID === valueId) {
                                value = val.value;
                            }
                        });
                        result[key] = value;
                    });
                    result = this.sortObjDict(result);
                });
                return result;
            },
            getSortedComponentDataFromDict(obj) {
                let dictCode = obj.dictCode;
                let keyCode = obj.keyCode;
                let valueCode = obj.valueCode;
                let parentCode = obj.parentCode;
                let result = {};
                let baseUrl = 'http://lis.nce.kz/Synergy/rest/api/dictionary/get_by_code?dictionaryCode=' + dictCode;
                axios.get(baseUrl, {
                    auth: {
                        username: 'admincrm',
                        password: 'Adm1nCRM',
                    }
                }).then((res) => {
                    let data = res.data;
                    let keyId;
                    let valueId;
                    let parentId;
                    data.columns.forEach((value) => {
                        if (value.code === keyCode) {
                            keyId = value.columnID;
                        }
                        if (value.code === valueCode) {
                            valueId = value.columnID;
                        }
                        if (value.code === parentCode) {
                            parentId = value.columnID;
                        }
                    });
                    let items = data.items;
                    items.forEach((item) => {
                        let key;
                        let value;
                        let parentKey;
                        item.values.forEach((val) => {
                            if (val.columnID === keyId) {
                                key = val.value;
                            }
                            if (val.columnID === valueId) {
                                value = val.value;
                            }
                            if (val.columnID === parentId) {
                                parentKey = val.value;
                            }
                        });
                        result[key] = {
                            value: value,
                            parentKey: parentKey
                        };
                    });
                });
                return result;
            },
            sortForSelect() {
                let result = [];
                for(let item in this.states) {
                    console.log(item);
                    let filKey = item;
                    let filValue = this.states[item];
                    for (let key in this.regions) {
                        let otParentKey = this.regions[key].parentKey;
                        if (parseInt(filKey) !== parseInt(otParentKey)) {
                            continue;
                        }
                        let otKey = key;
                        let otValue = this.regions[key].value;
                        result.push({
                            label: filValue + "\n" + otValue,
                            id: filKey + "/" + otKey
                        })
                    }
                }
                return  result;
            },
            sortObjDict(obj) {
                let _obj = obj;
                for (let cmp in _obj) {
                    if (parseInt(cmp) === 0) {
                        delete _obj[cmp];
                    }
                }
                return _obj;
            }
        },
        computed: {
            startDate: {
                get: function () {
                    if (!this.startDdate) {
                        let date = new Date();
                        return new Date(date.getFullYear(), date.getMonth(), 1).toISOString().substr(0, 10);
                    }
                    return this.startDdate;
                },
                set: function (val) {
                    this.startDdate = val;
                }

            }
        },
        created() {
            this.states = this.getComponentDataFromDict({
                dictCode: 'nce_dict_picture_branch',
                keyCode: 'nce_dict_picture_branch_key',
                valueCode: 'nce_dict_picture_branch_value'
            });
            this.regions = this.getSortedComponentDataFromDict({
                dictCode: 'nce_dict_picture_department',
                keyCode: 'nce_dict_picture_department_key',
                valueCode: 'nce_dict_picture_department_value',
                parentCode: 'nce_dict_picture_branch_key'
            });
            this.objRazdelList = this.getComponentDataFromDict({
                dictCode: 'nce_dict_picture_object_section',
                keyCode: 'nce_dict_picture_object_section_key',
                valueCode: 'nce_dict_picture_object_section_value'
            });
            this.objPodRazdelList = this.getSortedComponentDataFromDict({
                dictCode: 'nce_dict_picture_object_subsection',
                keyCode: 'nce_dict_picture_object_subsection_key',
                valueCode: 'nce_dict_picture_object_subsection_value',
                parentCode: 'nce_dict_picture_object_section_key'
            });
            this.objList = this.getSortedComponentDataFromDict({
                dictCode: 'nce_dict_picture_object',
                keyCode: 'nce_dict_picture_object_key',
                valueCode: 'nce_dict_picture_object_value',
                parentCode: 'nce_dict_picture_object_subsection_key'
            });
            this.labList = this.getComponentDataFromDict({
                dictCode: 'nce_dict_picture_lab',
                keyCode: 'nce_dict_picture_lab_key',
                valueCode: 'nce_dict_picture_lab_value'
            });
            this.areas = this.getComponentDataFromDict({
                dictCode: 'nce_dict_picture_areas',
                keyCode: 'nce_dict_picture_areas_key',
                valueCode: 'nce_dict_picture_areas_value'
            });
        },
    }
</script>

<style>
</style>