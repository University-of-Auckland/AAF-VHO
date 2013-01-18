package aaf.vhr

import org.springframework.dao.DataIntegrityViolationException
import org.apache.shiro.SecurityUtils

import aaf.base.identity.Role

class OrganizationController {

  static defaultAction = "list"
  static allowedMethods = [save: "POST", update: "POST", delete: "DELETE"]

  def beforeInterceptor = [action: this.&validOrganization, except: ['list', 'create', 'save']]

  def organizationService

  def list() {
    log.info "Action: list, Subject: $subject"
    [organizationInstanceList: Organization.list(params), organizationInstanceTotal: Organization.count()]
  }

  def show(Long id) {
    log.info "Action: show, Subject: $subject"
    def organizationInstance = Organization.get(id)

    def role = Role.findWhere(name:"organization:${organizationInstance.id}:administrators")
    [organizationInstance: organizationInstance, role:role]
  }

  def create() {
    if(SecurityUtils.subject.isPermitted("app:administrator")) {
      log.info "Action: create, Subject: $subject"
      [organizationInstance: new Organization()]
    }
    else {
      log.warn "Attempt to do administrative Organization create by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def save() {
    if(SecurityUtils.subject.isPermitted("app:administrator")) {
      def frID = params.frID
      if(frID instanceof String)
        frID = frID.toLong()

      def (created, organizationInstance) = organizationService.create(params.name, params.displayName, params.description, frID)

      if (!created && organizationInstance.hasErrors()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.save.failed'
        render(view: "create", model: [organizationInstance: organizationInstance])
        return
      } else {
        if(!created) {
          response.sendError 500
          return
        }
      }

      log.info "Action: save, Subject: $subject, Object: $organizationInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.organization.save.success'
      redirect(action: "show", id: organizationInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Organization save by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def edit(Long id) {
    def organizationInstance = Organization.get(id)

    if(SecurityUtils.subject.isPermitted("app:manage:organization:${organizationInstance.id}:edit")) {

      if(!organizationInstance.functioning()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.edit.not.functioning'

        render(view: "show", model: [organizationInstance: organizationInstance])
        return
      }

      log.info "Action: edit, Subject: $subject, Object: organizationInstance"
      [organizationInstance: organizationInstance]
    }
    else {
      log.warn "Attempt to do administrative Organization edit by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def update(Long id, Long version) {
    def organizationInstance = Organization.get(id)
    if(SecurityUtils.subject.isPermitted("app:manage:organization:${organizationInstance.id}:edit")) {

      if(!organizationInstance.functioning()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.update.not.functioning'
        render(view: "show", model: [organizationInstance: organizationInstance])
        return
      }
      
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.update.noversion'
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      if (organizationInstance.version > version) {
        organizationInstance.errors.rejectValue("version", "controllers.aaf.vhr.organization.update.optimistic.locking.failure")
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      if(SecurityUtils.subject.isPermitted("app:administration"))
        bindData(organizationInstance, params, [include: ['name', 'displayName', 'description', 'subjectLimit', 'groupLimit']])
      else
        bindData(organizationInstance, params, [include: ['name', 'displayName', 'description']])

      if (!organizationInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.update.failed'
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      log.info "Action: update, Subject: $subject, Object: $organizationInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.organization.update.success'
      redirect(action: "show", id: organizationInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Organization update by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def delete(Long id) {
    if(SecurityUtils.subject.isPermitted("app:administrator")) {
      def organizationInstance = Organization.get(id)
      try {
        organizationInstance.delete()

        log.info "Action: delete, Subject: $subject, Object: organizationInstance"
        flash.type = 'success'
        flash.message = 'controllers.aaf.vhr.organization.delete.success'
        redirect(action: "list")
      }
      catch (DataIntegrityViolationException e) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.delete.failure'
        redirect(action: "show", id: id)
      }
    }
    else {
      log.warn "Attempt to do administrative Organization delete by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def toggleActive(Long id, Long version) {
    if(SecurityUtils.subject.isPermitted("app:manage:organization:$id:edit")) {
      def organizationInstance = Organization.get(id)
      
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.toggleactive.noversion'
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      if (organizationInstance.version > version) {
        organizationInstance.errors.rejectValue("version", "controllers.aaf.vhr.organization.toggleactive.optimistic.locking.failure")
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      organizationInstance.active = !organizationInstance.active

      if (!organizationInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.organization.toggleactive.failed'
        render(view: "edit", model: [organizationInstance: organizationInstance])
        return
      }

      log.info "Action: toggleActive, Subject: $subject, Object: $organizationInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.organization.toggleactive.success'
      redirect(action: "show", id: organizationInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Organization toggleactive by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  private validOrganization() {
    if(!params.id) {
      log.warn "ID was not present"

      flash.type = 'info'
      flash.message = message(code: 'controllers.aaf.vhr.organization.no.id')

      redirect action:'list'
      return false
    }

    def organizationInstance = Organization.get(params.id)
    if (!organizationInstance) {
      log.warn "organizationInstance was not a valid instance"

      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.organization.notfound'

      redirect action:'list'
      return false
    }
  }
}
