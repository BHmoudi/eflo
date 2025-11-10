'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useMutation } from '@tanstack/react-query';
import TemplateEditor from '@/components/templates/TemplateEditor';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { ArrowLeft, Save } from 'lucide-react';
import toast from 'react-hot-toast';

export default function CreateTemplatePage() {
  const router = useRouter();
  const [showConfigDialog, setShowConfigDialog] = useState(true);
  const [htmlContent, setHtmlContent] = useState('');
  const [cssContent, setCssContent] = useState('');

  const [config, setConfig] = useState({
    templateCode: '',
    templateName: '',
    templateType: 'FEUILLE_GESTION',
    description: '',
    templateFormat: 'HTML',
    language: 'FR',
    applicableOrderTypes: 'VN',
    requiresApproval: true,
    approvalRoles: 'CDV,SALES_MANAGER',
    blocksWorkflowProgression: true,
  });

  const createMutation = useMutation({
    mutationFn: async (data: any) => {
      const res = await fetch('/api/templates', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });
      if (!res.ok) throw new Error('Failed to create template');
      return res.json();
    },
    onSuccess: () => {
      toast.success('Template created successfully');
      router.push('/dashboard/templates');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create template');
    },
  });

  const handleSave = (html: string, css: string) => {
    const fullHtml = css ? `<style>${css}</style>\n${html}` : html;

    createMutation.mutate({
      ...config,
      templateContent: btoa(fullHtml), // Base64 encode
    });
  };

  const handleConfigSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setShowConfigDialog(false);
  };

  return (
    <div className="h-screen flex flex-col">
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <Breadcrumb
          items={[
            { label: 'Dashboard', href: '/dashboard' },
            { label: 'Templates', href: '/dashboard/templates' },
            { label: 'Create Template' },
          ]}
        />
        <div className="flex items-center justify-between mt-4">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => router.push('/dashboard/templates')}
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back
            </Button>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Create Document Template</h1>
              {config.templateName && (
                <p className="text-sm text-gray-600 mt-1">{config.templateName}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {showConfigDialog ? (
        <div className="flex-1 flex items-center justify-center bg-gray-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl p-6">
            <h2 className="text-xl font-semibold mb-6">Template Configuration</h2>
            <form onSubmit={handleConfigSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="Template Code *"
                  value={config.templateCode}
                  onChange={(e) => setConfig({ ...config, templateCode: e.target.value.toUpperCase() })}
                  placeholder="FEUILLE_GESTION_VN"
                  required
                />
                <Input
                  label="Template Name *"
                  value={config.templateName}
                  onChange={(e) => setConfig({ ...config, templateName: e.target.value })}
                  placeholder="Feuille de Gestion VN"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea
                  className="w-full px-3 py-2 border rounded-lg"
                  rows={3}
                  value={config.description}
                  onChange={(e) => setConfig({ ...config, description: e.target.value })}
                  placeholder="Document description..."
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Template Type *</label>
                  <select
                    value={config.templateType}
                    onChange={(e) => setConfig({ ...config, templateType: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                    required
                  >
                    <option value="FEUILLE_GESTION">Feuille de Gestion</option>
                    <option value="BON_COMMANDE">Bon de Commande</option>
                    <option value="CONTRAT_VENTE">Contrat de Vente</option>
                    <option value="FACTURE">Facture</option>
                    <option value="DEVIS">Devis</option>
                    <option value="CUSTOM">Custom</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">Order Types</label>
                  <input
                    type="text"
                    value={config.applicableOrderTypes}
                    onChange={(e) => setConfig({ ...config, applicableOrderTypes: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                    placeholder="VN,VO,EVO"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Approval Roles (comma-separated)</label>
                <Input
                  value={config.approvalRoles}
                  onChange={(e) => setConfig({ ...config, approvalRoles: e.target.value })}
                  placeholder="CDV,SALES_MANAGER,ADMIN"
                  helperText="Roles from user-service that can approve this document"
                />
              </div>

              <div className="flex gap-4">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={config.requiresApproval}
                    onChange={(e) => setConfig({ ...config, requiresApproval: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <span className="text-sm">Requires Approval</span>
                </label>

                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={config.blocksWorkflowProgression}
                    onChange={(e) => setConfig({ ...config, blocksWorkflowProgression: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <span className="text-sm">Blocks Workflow Until Approved</span>
                </label>
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <Button type="button" variant="outline" onClick={() => router.push('/dashboard/templates')}>
                  Cancel
                </Button>
                <Button type="submit" variant="primary">
                  Continue to Editor
                </Button>
              </div>
            </form>
          </div>
        </div>
      ) : (
        <div className="flex-1 overflow-hidden">
          <TemplateEditor
            onChange={(html, css) => {
              setHtmlContent(html);
              setCssContent(css);
            }}
            onSave={handleSave}
          />
        </div>
      )}
    </div>
  );
}
