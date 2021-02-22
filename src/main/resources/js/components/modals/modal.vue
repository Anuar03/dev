<template>
    <div class="modal-box">
        <div class="input-group flex-nowrap">
            <div class="input-group-prepend">
                <span class="input-group-text" id="addon-wrapping">@</span>
            </div>
            <input type="text" class="form-control" placeholder="Username" aria-label="Username" aria-describedby="addon-wrapping">
        </div>
    </div>
</template>

<script>
    export default {
        name: 'Modal'
    }
</script>

<style>
.modal-box {
    height: 150px;
    width: 150px;
}
</style>