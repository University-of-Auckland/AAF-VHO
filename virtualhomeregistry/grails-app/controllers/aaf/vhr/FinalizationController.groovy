package aaf.vhr

import groovy.time.TimeCategory
import aaf.base.identity.SessionRecord

class FinalizationController {

  def managedSubjectService
  
  def index(String inviteCode) {
    def invitationInstance = ManagedSubjectInvitation.findWhere(inviteCode:inviteCode)

    if(!invitationInstance) {
      log.error "no such invitation exists"
      response.sendError 500
    }

    if(invitationInstance.utilized) {
      redirect action: 'used'
      return
    }

    [managedSubjectInstance:invitationInstance.managedSubject, invitationInstance:invitationInstance]
  }

  def loginAvailable(String login) {
    if(login.contains(' '))
      render "false"

    def managedSubjectInstance = ManagedSubject.findWhere(login:login)
    if(managedSubjectInstance)
      render "false"
    else
      render "true"
  }

  def complete(String inviteCode, String login, String plainPassword, String plainPasswordConfirmation) {
    def invitationInstance = ManagedSubjectInvitation.findWhere(inviteCode:inviteCode)

    if(!invitationInstance) {
      log.error "no such invitation exists"
      response.sendError 500
    }

    def (outcome, managedSubjectInstance) = managedSubjectService.finalize(invitationInstance, login, plainPassword, plainPasswordConfirmation)
    if(!outcome) {
      render (view: 'index', model:[managedSubjectInstance:invitationInstance.managedSubject, invitationInstance:invitationInstance])
    }
    
  }

  def used() {
  }

}
