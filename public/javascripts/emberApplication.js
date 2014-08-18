App = Ember.Application.create({});

App.ApplicationAdapter = DS.RESTAdapter.extend({
    namespace: 'rest'
});

App.ValidationUtils = {
    validateEmail : function(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }
};

App.Router.map(function() {
    this.route("lottery", { path : "/lotterie"});
    this.route("newUnboundGift", { path : "/faire-un-don"});
    this.route("unboundGiftThanks", { path : "/merci"});
    this.route("newLotteryParticipation", { path : "/participer-a-la-lotterie"});
    this.route("lotteryParticipationThanks", { path : "/merci-participation"})
});

App.UnboundGift = DS.Model.extend({
    lastName: DS.attr('string'),
    firstName:  DS.attr('string'),
    emailAddress: DS.attr('string'),
    amount: DS.attr('number', {defaultValue : 0}),
    message: DS.attr('string'),
    validate : function(){
        this.validateEmailAddress();
        this.validateAmount();
        return this.get('isValid');
    },
    validateEmailAddress : function(){
        if(!App.ValidationUtils.validateEmail(this.get('emailAddress'))){
            this.get('errors').add('emailAddress', 'Email non valide');
        }
    },
    validateAmount : function(){
        var parsedNumber = parseInt(this.get('amount'));
        if(isNaN(parsedNumber) || parsedNumber <= 0){
            this.get('errors').add('amount', "Ce montant n'est pas valide");
        }
    }
});

App.LotteryParticipation = App.UnboundGift.extend({
    nbrTickets: DS.attr('number'),
    nbrPacks: DS.attr('number'),
    validateNbrTickets : function(){
        if(parseInt(this.get('nbrTickets')) <= 0){
            this.get('errors').add('nbrTickets', "Ce montant n'est pas valide");
        }
    },
    validateNbrPacks : function(){
        if(parseInt(this.get('nbrPacks')) <= 0){
            this.get('errors').add('nbrPacks', "Ce montant n'est pas valide");
        }
    },
    validate : function(){
        this.validateNbrTickets();
        this.validateNbrPacks();
        return this._super();
    },
    nbrTicketsOrPacksChanged : function(){
        this.set('amount', ((this.get('nbrTickets') * 10) || 0) + ((this.get('nbrPacks') * 50) || 0))
    }.observes('nbrTickets', 'nbrPacks')
});

App.NewUnboundGiftRoute = Ember.Route.extend({
    model : function(){
        return this.store.createRecord('unboundGift', {})
    }
});

App.NewUnboundGiftController = Ember.ObjectController.extend({
    globalError : false,
    actions : {
        send : function(){
            var that = this;
            this.set('globalError', false);
            this.get('errors').clear();
            if(this.get('model').validate()){
                this.get('model').save().then(function(){
                    that.transitionToRoute('unboundGiftThanks');
                }, function(){
                    that.set('globalError', true);
                });
            }
        }
    }
});

App.NewLotteryParticipationRoute = Ember.Route.extend({
    model : function(){
        return this.store.createRecord('lotteryParticipation', {})
    }
});

App.NewLotteryParticipationController = Ember.ObjectController.extend({
    globalError : false,
    actions : {
        send : function(){
            var that = this;
            this.set('globalError', false);
            this.get('errors').clear();
            if(this.get('model').validate()){
                if(isNaN(this.get('nbrTickets'))) this.set('nbrTickets', 0);
                if(isNaN(this.get('nbrPacks'))) this.set('nbrPacks', 0);
                this.get('model').save().then(function(){
                    that.transitionToRoute('lotteryParticipationThanks');
                }, function(){
                    that.set('globalError', true);
                });
            }
        }
    }
});