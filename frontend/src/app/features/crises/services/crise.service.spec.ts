import { TestBed } from '@angular/core/testing';

import { CriseService } from './crise.service';

describe('CriseService', () => {
  let service: CriseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CriseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
