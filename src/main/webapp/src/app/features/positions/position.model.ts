export interface Position {
  id: string;
  positionRelevanceType: string | null;
  objectId: string | null;
  status: string | null;
  objectCode: string | null;
  objectDescription: string | null;
  positionValue: number | null;
  departmentId: string | null;
  organizationUnit: string | null;
  tariffGroup: string | null;
  baseGrade: string | null;
  percentage: number | null;
  startDate: string | null;
  endDate: string | null;
  fund: string | null;
  departmentId2: string | null;
  personnelNumber: string | null;
  employeeGroup: string | null;
  employeeCircle: string | null;
  entryDate: string | null;
  expectedExitDate: string | null;
  researchGroupId: string | null;
}

export interface ImportResult {
  message: string;
  count: number;
}

export interface EmployeeAssignment {
  personnelNumber: string;
  percentage: number;
  startDate: Date;
  endDate: Date;
  originalPosition: Position;
}

export interface GroupedPosition {
  objectId: string;
  objectCode: string | null;
  objectDescription: string | null;
  tariffGroup: string | null;
  positionValue: number;
  positionRelevanceType: string | null;
  organizationUnit: string | null;
  assignments: EmployeeAssignment[];
  dateRange: { start: Date; end: Date };
}

export interface TimeSlice {
  start: Date;
  end: Date;
  totalFillPercentage: number;
  assignments: EmployeeAssignment[];
}

