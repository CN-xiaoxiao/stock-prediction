import {defineStore} from "pinia";

export const useStore = defineStore('general', {
    state: () => {
        return {
            user: {
                role: '',
                username: '',
                email: '',
                img: ''
            }
        }
    },
    getters: {
        isAdmin() {
            return this.user.role === 'admin'
        }
    },
    persist: true
})
