package pollev

import groovy.cli.picocli.CliBuilder
import java.time.LocalDate
import io.github.bonigarcia.wdm.WebDriverManager
import geb.Browser
//import geb.Page


class Main{

    static void main (String... args){
        def cli = new CliBuilder(usage:'pollev [options]', header: 'Options:')
        cli.h("print this message")
        cli.d(longOpt:'duration', args:1, argName: 'duration', 'Minutes to check pollev, default is 1 minutes.')
        cli.p(longOpt:'presenter', args:1, argName: 'presenter', 'Presenter to listen to.')
        cli.v(longOpt:'verbose', defaultValue:false, "show debug information.")
        cli.u(longOpt:'user', args:1, argName:'<username:password>', 'pollev login username and password')
        //cli.l(defaultValue:false, "list all modules and files to create")
        //cli.m(args:'+', optionalArg: true, valueSeparator:',', argName:'module', 'modules to process, default is all')
        
        def options = cli.parse(args)
        if(options.h || !args.size()){
            cli.usage()
            return
        }
        def params = [:]
/*
        if(options.arguments().size()>0){
            def command = new ProgramsImport(
                                serverUrl:'http://gbcdcu01u.gbcuat.local/InfosilemAcademicSuiteAPI/TimetablerImportExportServices.svc',
                                serverName: 'sqlsj-uat',
                                dbName: 'TEST222')
            println command.test()
            //command.results.each{
            //    println it
            //}
            println command.resultCode
            println command.resultErrorDetail
            return
        }
*/
        params.verbose = options.v
        
        if(options.u){
            def sa = options.u.split(':')
            params.user = sa[0]
            if(sa.size()>1) params.password = sa[1]
        }
        if(options.p) {
            params.presenter = options.p
        }
        params.duration = 60
        if(options.d) {
            params.duration = 60*(options.d as int)
        }

        println "presenter:         ${params.presenter}"
        println "duration:          ${params.duration} seconds"
        println "user:              ${params.user}"
        //println "password:          ${params.password}"


        WebDriverManager.chromedriver().setup()
        Browser.drive {
            go "https://pollev.com/login"
            waitFor { title == "Log in or create an account" }

            $("input", class: "component-element-text-field__input", name: "username") << params.user //'sifei.li@mail.utoronto.ca'
            $("button.component-auth__submit")[0].click()
            
            waitFor { $("input", class: "component-element-text-field__input", name: "password") }
            $("input", class: "component-element-text-field__input", name: "password") << params.password //'lisifei158'
            $("button.component-auth__submit")[0].click()

            waitFor { $("input", class: "text-field__input") }
            $("input", class: "text-field__input") << params.presenter //'brianli819' //'vzhang1'
            $("button.join--button").click()

            waitFor { $("button.element-button--link") }
            $("button.element-button--link").click()

            waitFor { title.contains(params.presenter) }

            String question = $("div.component-response-header__title").text()
            def startTime = new Date().getTime()

            
            while ((new Date().getTime()-startTime)/1000 < params.duration){
                Thread.sleep(1000)
                //waitFor { $("div.component-response-header__title") }
                //println $("div.component-response-header__title").text()
                if ($("div.component-response-header__title").text() != question){
                    question = $("div.component-response-header__title").text()
                    if(question){
                        $("button.component-response-multiple-choice__option")[1].click()
                        println "$question answered."
                    }else{
                        println "question is changed to $question."
                    }
                }
            }
        }
        println 'Done.'
        //System.in.read();

        return
    }
}