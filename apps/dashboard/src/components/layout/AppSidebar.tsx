import * as React from 'react'
import { Link, useLocation } from '@tanstack/react-router'
import { Collapsible } from '@base-ui/react/collapsible'
import { ChevronRightIcon } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { Logo } from '#/components/brand/logo'
import { useDirection } from '#/hooks/use-direction'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuBadge,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  SidebarSeparator,
  useSidebar,
} from '#/components/ui/sidebar'
import type { SidebarConfig } from './sidebarConfig'
import NavUser from './NavUser'

type AppSidebarProps = {
  config: SidebarConfig
}

export function AppSidebar({ config }: AppSidebarProps) {
  const { state, isMobile, setOpenMobile } = useSidebar()
  const { pathname } = useLocation()
  const direction = useDirection()
  const { t } = useTranslation('navbar')

  const handleNavClick = React.useCallback(() => {
    if (isMobile) {
      setOpenMobile(false)
    }
  }, [isMobile, setOpenMobile])

  // Automatically close mobile sidebar on any route transition
  React.useEffect(() => {
    if (isMobile) {
      setOpenMobile(false)
    }
  }, [pathname, isMobile, setOpenMobile])

  const isActive = (url: string) =>
    pathname === url || pathname.startsWith(`${url}/`)

  const itemLabel = (key: string) => t(`items.${key}`)

  return (
    <Sidebar collapsible="icon" side={direction === 'rtl' ? 'right' : 'left'}>
      <SidebarHeader className="h-14 justify-center px-2 group-data-[collapsible=icon]:px-0">
        <div
          className="flex items-center group-data-[collapsible=icon]:justify-center"
          onClick={handleNavClick}
        >
          <Logo size="xs" scale={1.3} hideWordmark={state === 'collapsed'} />
        </div>
      </SidebarHeader>
      <SidebarSeparator className="mx-0" />

      <SidebarContent>
        {config.map((section) => {
          if (section.type === 'collapsible') {
            return (
              <SidebarGroup key={section.labelKey}>
                <SidebarGroupLabel>{t(section.labelKey)}</SidebarGroupLabel>
                <SidebarGroupContent>
                  <SidebarMenu>
                    <Collapsible.Root defaultOpen className="group/collapsible">
                      <SidebarMenuItem>
                        <Collapsible.Trigger
                          render={
                            <SidebarMenuButton
                              tooltip={t(section.triggerKey)}
                            />
                          }
                        >
                          <section.icon />
                          <span>{t(section.triggerKey)}</span>
                          <ChevronRightIcon className="icon-flip ms-auto transition-transform duration-200 group-data-open/collapsible:rotate-90" />
                        </Collapsible.Trigger>
                        <Collapsible.Panel>
                          <SidebarMenuSub>
                            {section.items.map((item) => (
                              <SidebarMenuSubItem key={item.key}>
                                <SidebarMenuSubButton
                                  render={
                                    <Link
                                      to={item.url}
                                      onClick={handleNavClick}
                                    />
                                  }
                                  isActive={isActive(item.url)}
                                >
                                  <item.icon />
                                  <span>{itemLabel(item.key)}</span>
                                </SidebarMenuSubButton>
                              </SidebarMenuSubItem>
                            ))}
                          </SidebarMenuSub>
                        </Collapsible.Panel>
                      </SidebarMenuItem>
                    </Collapsible.Root>
                  </SidebarMenu>
                </SidebarGroupContent>
              </SidebarGroup>
            )
          }

          return (
            <SidebarGroup key={section.labelKey}>
              <SidebarGroupLabel>{t(section.labelKey)}</SidebarGroupLabel>
              <SidebarGroupContent>
                <SidebarMenu className="gap-1">
                  {section.items.map((item) => (
                    <SidebarMenuItem key={item.key}>
                      <SidebarMenuButton
                        render={
                          <Link to={item.url} onClick={handleNavClick} />
                        }
                        isActive={isActive(item.url)}
                        tooltip={itemLabel(item.key)}
                      >
                        <item.icon />
                        <span>{itemLabel(item.key)}</span>
                        {item.badge && (
                          <SidebarMenuBadge>{item.badge}</SidebarMenuBadge>
                        )}
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          )
        })}
      </SidebarContent>

      <SidebarFooter>
        <NavUser />
      </SidebarFooter>
    </Sidebar>
  )
}

export default AppSidebar
