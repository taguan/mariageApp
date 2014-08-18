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
});

App.UnboundGift = DS.Model.extend({
    lastName: DS.attr('string'),
    firstName:  DS.attr('string'),
    emailAddress: DS.attr('string'),
    amount: DS.attr('number'),
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
                    console.log('ok');
                    that.transitionToRoute('unboundGiftThanks');
                }, function(data){
                    console.log('error');
                    console.log(data);
                    that.set('globalError', true);
                });
            }
        }
    }
});