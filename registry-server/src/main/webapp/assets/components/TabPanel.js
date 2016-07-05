var React = require('react');
var $ = require("jquery");

var Tab = require('./Tab');
var TabContent = require('./TabContent');
//var services = require('./services.json');

module.exports = React.createClass({
    componentDidMount: function () {
        $.getJSON(root + "/api/services", function (data) {
            this.setState({
                services: data,
                currentTab: data[0]
            });
        }.bind(this));
    },
    getInitialState: function () {
        return {
            services: [],
            currentService: {}
        };
    },
    tabsClickHandler: function (tab) {
        var current = null;
        this.state.services.forEach(function (service) {
            if (service.name === tab) {
                current = service;
            }
        });
        this.setState({currentService: current});
    },
    render: function () {
        var that = this;
        var services = this.state.services.map(function (tab, i) {
            return <Tab clickHandler={that.tabsClickHandler} key={tab.name} name={tab.name}
                        current={tab === that.state.currentService}/>
        });
        return (
            <div id="main">
                <div id="cols" className="box padding">
                    <div id="content" className="box">
                        <h3 className="tit">Services</h3>
                        <div className="tabs box">
                            <ul className="ui-tabs-nav">

                                {services}

                            </ul>
                        </div>
                        <div >
                            <TabContent
                                instances={that.state.currentService ? [] : that.state.currentService.instances}/>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});