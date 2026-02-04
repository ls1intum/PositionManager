export interface UserSummary {
  id: string;
  universityId: string;
  firstName: string | null;
  lastName: string | null;
  email: string | null;
}

export interface ResearchGroup {
  id: string;
  name: string;
  abbreviation: string;
  description: string | null;
  websiteUrl: string | null;
  campus: string | null;
  department: string | null;
  professorFirstName: string | null;
  professorLastName: string | null;
  archived: boolean;
  head: UserSummary | null;
  aliases: string[];
  positionCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ResearchGroupFormData {
  name: string;
  abbreviation: string;
  description: string;
  websiteUrl: string;
  campus: string;
  department: string;
  professorFirstName: string;
  professorLastName: string;
  aliases: string[];
}

export interface ResearchGroupImportResult {
  created: number;
  updated: number;
  skipped: number;
  errors: string[];
  warnings: string[];
}

export interface BatchAssignResult {
  matched: Record<string, string>;
  unmatchedOrgUnits: string[];
}
