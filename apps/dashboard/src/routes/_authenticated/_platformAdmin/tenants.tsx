import { createFileRoute } from '@tanstack/react-router'
import { useTranslation } from 'react-i18next'
import PageHeader from '#/components/layout/PageHeader'
import { TenantsTable } from '#/feature/tenants/components/TenantsTable'

export const Route = createFileRoute('/_authenticated/_platformAdmin/tenants')({
  component: RouteComponent,
})

function RouteComponent() {
  const { t } = useTranslation('common')
  return (
    <>
      <PageHeader
        title={t('pages.tenants.title')}
        description={t('pages.tenants.description')}
      />
      <div className="mt-4">
        <TenantsTable />
      </div>
    </>
  )
}
