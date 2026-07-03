import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CriseListComponent } from './crise-list.component';

describe('CriseListComponent', () => {
  let component: CriseListComponent;
  let fixture: ComponentFixture<CriseListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CriseListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CriseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
