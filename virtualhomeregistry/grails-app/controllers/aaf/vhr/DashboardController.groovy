package aaf.vhr

import groovy.time.TimeCategory
import aaf.base.identity.SessionRecord

class DashboardController {
  
  static defaultAction = "dashboard"

  def welcome = {
    if(subject) {
      redirect action:'dashboard'
    }
  }

  def dashboard = {
    def organizationInstanceList = [] as List
    def groupInstanceList = [] as List
    def statistics = [:]
      
    use(TimeCategory) {
      subject.roles?.each { role -> 
        if(role.name.startsWith('organization:')) {
          def components = role.name.split(':')
          if(components.size() == 3) {
            def organization = Organization.get(components[1])
            organizationInstanceList << organization
          }
        }
        if(role.name.startsWith('group:')) {
          def components = role.name.split(':')
          if(components.size() == 3) {
            def group = Group.get(components[1])
            groupInstanceList << group
          }
        }
      }
      
      statistics.organizations = Organization.count()
      statistics.groups = Group.count()
      statistics.managedSubjects = ManagedSubject.count()

      def queryParams = [:]
      queryParams.endDate = new Date()
      queryParams.startDate = queryParams.endDate - 12.month
      def last12MonthSessions = SessionRecord.executeQuery("select count(*) from SessionRecord where dateCreated between :startDate and :endDate group by month(dateCreated)", queryParams)
    
      def monthsCovered = last12MonthSessions.size()
      if( monthsCovered < 12) {
        while ( monthsCovered++ < 12 ) {
            last12MonthSessions.add(0,0)
        }  
      }

      statistics.last12MonthSessions = last12MonthSessions
    }

    [organizationInstanceList:organizationInstanceList, groupInstanceList: groupInstanceList, statistics:statistics]
  }

}
