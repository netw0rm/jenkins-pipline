stage 'init 初始化'


@NonCPS
def getjobname(text){
    def patten='(.*)-(.*)-(.*)$'
    def matcher= text=~patten
    matcher.matches()
    def job_name
    job_name=matcher[0][1]
    return job_name
}

@NonCPS
def getcontrolname(corename){
    def result
    if(corename[-4..-1] == 'core'){
        result = corename[0..-5] + 'control'
    }else{
        def patten='(.*)core_(.*)'
        def matcher= corename=~patten
        matcher.matches()
        def appname=matcher[0][1] 
        def sdname=matcher[0][2]
        result = appname +'control_'+ sdname 
    } 
    return result
}

@NonCPS
def getreg(text,reg){
   def patten='.*' + reg + '.*'
   def result= text=~patten
   return result.matches()
}
@NonCPS
def getsdcontrolname(corename){
   def names= corename.split('_')
   def firstname=names[0]
   def patten='.*' + core + '.*'
   def result= text=~patten
   return result.matches()
}

@NonCPS
def getlayer(text){

    def result
    if(getreg(text,'core'))
        result='core'
    else if(getreg(text,'control'))
        result = 'control'
    else if(getreg(text,'wf_nbpm'))
        result = 'core'
    else if(getreg(text,'wf_status'))
        result = 'core'
    else if(getreg(text,'crmpfmq'))
        result = 'core'
    else if(getreg(text,'crmpftask'))
        result = 'core'
    else if(getreg(text,'crmpfesb'))
        result = 'core'
    else if(getreg(text,'wf_platform'))
        result = 'control'
    else if(getreg(text,'ngmmserver'))
        result = 'control'
    else
        result = 'view'
    return result
}

@NonCPS
def getProject(text){
    def line = text.split('/')
    def project = ['name':line[4],'tag':line[-1]]
    return project
}

def getpkgname(text){
    def jars=['csf-core',
          'csf-admin',
          'wf_nbpm',
          'wf_status',
          'wf_platform',
          'ngtask',
          'ngtask-core',
    ]
    def pkgname
    if (jars.contains(text)){
        pkgname='jar'
    }else{
        pkgname='war'
    }
    return pkgname
}

def isinternetview(app){
    def apps=['ngmmgw','crmpfesb']
    return apps.contains(app)
}
def isinternetapi(app){
    def apps=['ngmmgwsentcenter']
    return apps.contains(app)
}
def isinterfaceapi(app){
    def apps=['ngcctcontrol','ngcscontrol','ngcslogcontrol',
              'ngmttcontrol','ngwfcontrol_gx','ngmmrevcenter',
              'ngmmgwsentcenter'
             ]
    return apps.contains(app)
}
def isinterfaceview(app){
    def apps=['crmpfesb','crmpfweb','csf-admin',
              'logweb','nganoce','ngba',
              'ngbusi_gx','ngcct','ngcslog',
              'ngcs','ngmm','ngtask',
              'ngwf_gx','ngwf','wf-manager',
              'ngmtt'
             ]
    return apps.contains(app)
}


def getdepcmd(app,group){
    def pkgname = getpkgname(app)
    def command=''
    def tag_num='1'
    if(group == 'B'){
       tag_num='2'
    }
    if('jar' == pkgname){
    //jar发布
        command = '/home/jenkins/bin/ansplayd playbooks/'
        command = command + app + '_dep.yml -e "hosts=' + app + tag_num
        command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
        command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
    } else {
        if ('wf_manager' == app){
        //wf_manager发布
            command = '/home/jenkins/bin/ansplayd playbooks/'
            command = command + app + '_dep.yml -e "hosts=' + app + tag_num
            command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
            command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
        } else {
            //war包发布
             command = '/home/jenkins/bin/ansplayd playbooks/tomdep.yml -e "hosts=' + app + tag_num
             command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
             command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
        }
    }
    echo command
    return command
}

def getdep7cmd(app){
    def pkgname = getpkgname(app)
    def command=''
    
    if('jar' == pkgname){
    //jar发布
        command = '/home/jenkins/bin/ansplay7 playbooks/'
        command = command + app + '_dep.yml -e "hosts=' + app 
        command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
        command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
    } else {
        if ('wf_manager' == app){
        //wf_manager发布
            command = '/home/jenkins/bin/ansplayd playbooks/'
            command = command + app + '_dep.yml -e "hosts=' + app + tag_num
            command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
            command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
        } else {
            //war包发布
             command = '/home/jenkins/bin/ansplay7 playbooks/tomdepline7.yml -e "hosts=' + app 
             command = command + ' buildpath=/home/jenkins/.jenkins/workspace/rhkf_prd/'
             command = command + app + ' app=' + app +' pkg=' + app + '.' + pkgname + '"'
        }
    }
    echo command
    return command
}

def getnginxcmd(app,layer,tag){
    def command = '~/bin/ansd -vvv nginx-' + layer +' -m shell -a "source ~/.bash_profile; sh ~/shell/nginx_kvm.sh '
    command = command + app + ' ' + tag + '"'
    return command
}

def fromcoretocontrol(core){
    def result = ''
    switch(core){
        case 'crmpfcore':
             result='crmpfcore'
             break
        case 'crmpftask':
             result='crmpfcore'
             break
        case 'crmpfmq':
             result='crmpfcore'
             break
        case 'crmpfesb':
             result='crmpfcore'
             break
        case 'wf-nbpm':
             result='wf-platform'
             break
        case 'wf-status':
             result='wf-platform'
             break
        default:
            result = getcontrolname(core)
    }
    return result
}

def branches = [:]
def x = "${tags}"
def apps=  x.split('\n')
def cores=[]
def controls=[]
def views=[]
def nginxapis=[]



stage '构建'

for (int i=0;i<apps.size();i++){
    def project = getProject(apps[i])
    def jobname = '/rhkf_prd/' + project.name    
    def tag = project.tag
    branches[project.name] ={
        build job:jobname, parameters: [[$class: 'GitParameterValue', name: 'BUILD_BRANCH', value: project.tag],[$class: 'StringParameterValue', name: 'APP_NAME', value: project.name],[$class: 'StringParameterValue', name: 'ISAUTO', value: 'true']]
    }
    //分层
    switch(getlayer(project.name)){
        case 'core':
            cores.add(project.name)
            break
        case 'control':
            controls.add(project.name)
            break
        default:
            views.add(project.name)
    }
}

//并行执行构建
def group = 'A'
if(group == 'A'){
  parallel branches
}

nginxapis = controls[]
//根据core的名称，把对应的rest 层添加到需要切掉A组的列表里
for(int i=0;i<cores.size();i++){
    //测试此列表有去重功能，若不能实现，则需改造
    nginxapis.add(fromcoretocontrol(cores[i]))
}

node {   
 
        stage 'nginx去掉 '+ group + '组'
        //接口域 互联网域view层去掉a组
        for (int i=0;i<views.size();i++){      
            if(isinterfaceview(views[i])){
                def command = getnginxcmd(views[i],'interface',group)
                sh command
            }
            if (isinternetview(views[i])) {
                def command = getnginxcmd(views[i],'internet',group)
                sh command
            }
        }
        stage '核心层rest api nginx去掉 '+ group +'组'
        for (int i=0;i<nginxapis.size();i++){
            def command = getnginxcmd(nginxapis[i],'core',group)
            sh command
        }

    
    
    
    
    //发布顺序为core control view
    stage '发布core '+ group + '组'    
    for (int i=0;i<cores.size();i++){
       sh getdep7cmd(cores[i])
       command = getdepcmd(cores[i],group)     
       sh command
    }
    //sleep
    stage 'sleep core 10s '
    sleep 20
    stage '发布control '+ group + '组'   
    for (int i=0;i<controls.size();i++){
       sh getdep7cmd(controls[i])
       command = getdepcmd(controls[i],group)     
       sh command
    }
    stage 'sleep control 10s '
    sleep 20
    stage '发布view '+ group + '组'
    for (int i=0;i<views.size();i++){
       sh getdep7cmd(views[i])
       command = getdepcmd(views[i],group)   
       sh command
    }
    stage 'sleep view 10s '
    sleep 20
    
}

//## B 组
group = 'B'
node {   
 
        stage 'nginx去掉 '+ group + '组'
        //接口域 互联网域view层去掉a组
        for (int i=0;i<views.size();i++){      
            if(isinterfaceview(views[i])){
                def command = getnginxcmd(views[i],'interface',group)
                sh command
            }
            if (isinternetview(views[i])) {
                def command = getnginxcmd(views[i],'internet',group)
                sh command
            }
        }
        stage '核心层rest api nginx去掉 '+ group +'组'
        for (int i=0;i<nginxapis.size();i++){
            def command = getnginxcmd(nginxapis[i],'core',group)
            sh command
        }

    
    
    
    
    //发布顺序为core control view
    stage '发布core '+ group + '组'    
    for (int i=0;i<cores.size();i++){
       command = getdepcmd(cores[i],group)     
       sh command
    }
    //sleep
    stage 'sleep core 10s '
    sleep 20
    stage '发布control '+ group + '组'   
    for (int i=0;i<controls.size();i++){
       command = getdepcmd(controls[i],group)     
       sh command
    }
    stage 'sleep control 10s '
    sleep 20
    stage '发布view '+ group + '组'
    for (int i=0;i<views.size();i++){
       command = getdepcmd(views[i],group)   
       sh command
    }
    stage 'sleep view 10s '
    sleep 20

    stage 'nginx 恢复ALL'

    for (int i=0;i<views.size();i++){       
        if(isinterfaceview(views[i])){
            def command = getnginxcmd(views[i],'interface','ALL')
            sh command
        }
        if (isinternetview(views[i])) {
            def command = getnginxcmd(views[i],'internet','ALL')
            sh command
        }
    }
    stage '核心层rest api nginx 恢复 ALL'
    for (int i=0;i<nginxapis.size();i++){
        def command = getnginxcmd(nginxapis[i],'core','ALL')
        sh command
    }

}

