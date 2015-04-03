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
    this.route("lottery", { path : "/loterie"});
    this.route("newUnboundGift", { path : "/faire-un-don"});
    this.route("informations", { path : "/informations"});
    this.route("nousenimages", { path : "/nous-en-images"});
    this.resource("messages", { path : "/boite-a-messages"});
    this.route("dresscode", { path : "/dress-code"});
    this.route("luiparelle", { path : "/luiparelle"});
    this.route("elleparlui", { path : "/elleparlui"});
    this.route("unboundGiftThanks", { path : "/merci"});
    this.route("newLotteryParticipation", { path : "/participer-a-la-lotterie"});
    this.route("lotteryParticipationThanks", { path : "/merci-participation"});
    this.route("confirmation", { path : "/confirmation"});
    this.route("confirmationOk", { path : "/merci-confirmation"});
    this.route("code", { path : "/entrer-code"});
    this.resource("tickets", { path : "/tickets/:participationCode"}, function(){
        this.route("show", { path : "/:ticketCode"})
    });
});

App.Message = DS.Model.extend({
    name: DS.attr('string'),
   message: DS.attr('string'),
    validate: function(){
        if(!this.get('name')){
            this.get('errors').add('name', 'Requis');
        }
        if(!this.get('message')){
            this.get('errors').add('message', 'Requis');
        }
        return this.get('isValid');
    }
});

App.Confirmation = DS.Model.extend({
    lastName: DS.attr('string'),
    firstName:  DS.attr('string'),
    isComing: DS.attr('boolean', {defaultValue : true}),
    nbrComing: DS.attr('number', {defaultValue : 0}),
    comment : DS.attr('string'),
    validate : function(){
        this.validateLastName();
        this.validateNbrComing();
        return this.get('isValid');
    },
    validateLastName : function(){
        if(!this.get('lastName')){
            this.get('errors').add('lastName', 'Requis');
        } else {
            this.set('lastName', $.trim(this.get('lastName')));
        }
    },
    validateNbrComing : function(){
        if(!this.get('nbrComing')) this.set('nbrComing', 0);
        var parsedNumber = parseInt($.trim(this.get('nbrComing')));
        if(isNaN(parsedNumber) || parsedNumber < 0){
            this.get('errors').add('nbrComing', "Non valide");
        }
        else {
            this.set('nbrComing', parsedNumber);
        }
    }
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
        if(!this.get('emailAddress')) this.get('errors').add('emailAddress', 'Email requis');
        else{
            var email = $.trim(this.get('emailAddress'));
            if(!App.ValidationUtils.validateEmail(email)){
                this.get('errors').add('emailAddress', 'Email non valide');
            }
            else{
                this.set('emailAddress', email);
            }
        }
    },
    validateAmount : function(){
        if(!this.get('amount')) this.set('amount', 0);
        var parsedNumber = parseInt($.trim(this.get('amount')));
        if(isNaN(parsedNumber) || parsedNumber <= 0){
            this.get('errors').add('amount', "Ce montant n'est pas valide");
        }
        else {
            this.set('amount', parsedNumber);
        }
    }
});

App.MessagesRoute = Ember.Route.extend({
    model: function(){
        return this.store.find('message');
    }
});


App.MessagesController = Ember.ArrayController.extend({
    name : null,
    message : null,
    globalError : false,
    actions : {
        send : function(){
            var that = this;
            this.set('globalError', false);
            if(this.get('name') && this.get('message')){
                var createdMessage = this.store.createRecord('message', {name : this.get('name'), message : this.get('message')});
                createdMessage.save().then(function(){
                    that.set('name', null);
                    that.set('message', null)
                }, function(){
                    that.set('globalError', true);
                });
            }
            else{
                this.set('globalError', true)
            }
        }
    }
});


App.LotteryParticipation = App.UnboundGift.extend({
    nbrTickets: DS.attr('number', {defaultValue : 0}),
    nbrPacks: DS.attr('number',  {defaultValue : 0}),
    validateNbrTickets : function(){
        if(!this.get('nbrTickets')) this.set('nbrTickets', 0);
        var parsedNumber = parseInt($.trim(this.get('nbrTickets')));
        if(isNaN(parsedNumber) || parsedNumber < 0){
            this.get('errors').add('nbrTickets', "Ce montant n'est pas valide");
        }
        else{
            this.set('nbrTickets', parsedNumber);
        }
    },
    validateNbrPacks : function(){
        if(!this.get('nbrPacks')) this.set('nbrPacks', 0);
        var parsedNumber = parseInt($.trim(this.get('nbrPacks')));
        if(isNaN(parsedNumber) || parsedNumber < 0){
            this.get('errors').add('nbrPacks', "Ce montant n'est pas valide");
        }
        else{
            this.set('nbrPacks', parsedNumber);
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

App.Ticket = DS.Model.extend();

App.CodeController = Ember.Controller.extend({
    generalError : false,
    participationCode : null,
    actions : {
        sendCode : function(){
            var self = this;
            this.set('generalError', false);
            if(this.get('participationCode')){
                this.store.find('ticket', { participationCode : this.get('participationCode')}).then(function(){
                    self.transitionToRoute('tickets', self.get('participationCode'));
                }).fail(function(){
                    self.set('generalError', true);
                })
            }
            else{
                this.set('generalError', true);
            }
        }
    }
});



App.TicketsRoute = Ember.Route.extend({
    model : function(params){
        return this.store.find('ticket', { participationCode : params.participationCode})
    }
});

App.TicketsController = Ember.ArrayController.extend({
    modelWithIndices: function() {
        return this.get('model').map(function(i, idx) {
            return {item: i, index: idx + 1};
        });
    }.property('model.@each')
});

App.TicketsShowRoute = Ember.Route.extend({
    model : function(params){
        return this.store.find('ticket', params.ticketCode)
    }
});

App.TicketsShowController = Ember.ObjectController.extend({
    imageUrl : function(){
        return "/ticket/image/" + this.get('id')
    }.property('id'),
    pdfUrl : function(){
        return "/ticket/pdf/" + this.get('id')
    }.property('id')
});

App.ConfirmationRoute = Ember.Route.extend({
    model : function(){
        return this.store.createRecord('confirmation', {})
    }
});

App.ConfirmationController = Ember.ObjectController.extend({
    globalError : false,
    actions : {
        send : function(){
            var that = this;
            this.set('globalError', false);
            this.get('errors').clear();
            if(this.get('model').validate()){
                this.get('model').save().then(function(){
                    that.transitionToRoute('confirmationOk');
                }, function(){
                    that.set('globalError', true);
                });
            }
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

App.ScratchImageView = Ember.View.extend({
    id: Ember.computed.alias('controller.model.id'),
    classNames: ['scratchImage'],
    attributeBindings: ['bgImage'],
    bgImage: function(){
        return "/ticket/image/" + this.get('id')
    }.property('id'),
    bgImageObserver: function(){
        this.rerender();
    }.observes('id'),
    didInsertElement: function(){
        this.$().wScratchPad({
            size        : 50,          // The size of the brush/scratch.
            bg          : this.get('bgImage'),  // Background (image path or hex color).
            fg          : '#91c898',  // Foreground (image path or hex color).
            realtime    : true,       // Calculates percentage in realitime.
            scratchDown : null,       // Set scratchDown callback.
            scratchUp   : null,       // Set scratchUp callback.
            scratchMove : null,       // Set scratcMove callback.
            cursor      : 'url("/assets/images/coin.png") 5 5, default' // Set cursor.
        });
    }
});
